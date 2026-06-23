# SPEC-ORDER-001: Crear pedido

## ID
SPEC-ORDER-001

## Título
Creación de pedido con detalles y enrutamiento a cocina

## Actor
`WAITER`, `ADMIN`

## Precondiciones
- Usuario autenticado con JWT válido
- Usuario tiene rol `WAITER` o `ADMIN` en el restaurante activo
- Header `x-restaurant-id` contiene ID del restaurante válido
- Todos los productos referenciados existen y están activos (`is_active = true`, `status = 'AVAILABLE'`)
- Si `type = 'IN_PLACE'`, la mesa existe y está disponible (`status = 'AVAILABLE'`)

## Flujo

1. Usuario envía `POST /orders` con header `x-restaurant-id: <restaurant-uuid>` y payload:
   ```json
   {
     "tableId": "uuid-mesa-12",      // opcional, obligatorio si type=IN_PLACE
     "clientId": "uuid-cliente",    // opcional
     "type": "IN_PLACE",            // IN_PLACE | TAKE_AWAY
     "people": 2,
     "notes": "Sin cebolla en la taco",
     "details": [
       {
         "productId": "uuid-producto-taco",
         "quantity": 3,
         "productOptionId": "uuid-opcion-con-todo",
         "description": "Bien dorados"
       }
     ]
   }
   ```

2. Backend valida:
   - Usuario tiene rol `WAITER` o `ADMIN` en el restaurante (RBAC)
   - Si `type = 'IN_PLACE'`: `tableId` es obligatorio
   - Mesa existe y tiene `status = 'AVAILABLE'` (no OCCUPIED)
   - Todos los `productId` existen, pertenecen al restaurante y están activos/disponibles
   - Cada `productOptionId` (si presente) existe y pertenece al producto
   - `quantity` > 0 para cada detalle

3. Backend calcula totales:
   - Para cada detalle: `amount = quantity * product.price + productOption.price` (si aplica)
   - `order.total = Σ(detail.amount)`

4. Backend genera `order.num`:
   - Consulta máximo `num` existente para el restaurante
   - Asigna `max_num + 1` (correlativo por restaurante, ver INV-01)

5. Backend persiste en transacción:
   - Crea `Order` con:
     - `status = 'PENDING'`
     - `status_pay = 'NO_PAY'`
     - `total` calculado
     - `restaurant_id` desde header
     - `user_id` desde JWT `sub` claim
   - Crea `OrderDetail` para cada item con:
     - `price` desde `product.price`
     - `amount` calculado
     - `status = 'PENDING'`
     - `product.production_area_id` copiado para enrutamiento

6. Backend actualiza estado de mesa:
   - Si `type = 'IN_PLACE'`, mesa pasa a `status = 'OCCUPIED'`

7. Backend emite evento WebSocket:
   - Topic: `/topic/restaurant/{restaurantId}/orders`
   - Evento: `order:created`
   - Payload: `Order` completo con detalles

8. Backend responde con `201 Created`:
   ```json
   {
     "id": "uuid-order-nuevo",
     "num": 42,
     "type": "IN_PLACE",
     "status": "PENDING",
     "statusPay": "NO_PAY",
     "people": 2,
     "total": 156.50,
     "notes": "Sin cebolla en la taco",
     "tableId": "uuid-mesa-12",
     "clientId": "uuid-cliente",
     "userId": "uuid-mesero",
     "isPaid": false,
     "isClosed": false,
     "createdAt": "2026-06-21T14:30:00Z",
     "details": [
       {
         "id": "uuid-detail-1",
         "productId": "uuid-producto-taco",
         "quantity": 3,
         "price": 45.00,
         "amount": 135.00,
         "status": "PENDING",
         "description": "Bien dorados"
       }
     ]
   }
   ```

## Postcondiciones
- Pedido persistido en `order` y `order_detail`
- Mesa marcada como OCCUPIED si `type = 'IN_PLACE'`
- Evento WebSocket broadcasteado a todos los clientes del mismo restaurante
- `order.num` es único dentro del restaurante
- `order.total` coincide con suma de detalles

## Invariantes

**INV-01:** `order.num` es único por restaurante. No puede existir otro pedido con el mismo `(restaurant_id, num)`.

**INV-04:** `order.total = Σ(order_detail.amount)` siempre consistente. Cualquier modificación de detalles debe recalcular el total.

**INV-ORDER-001:** Un pedido con `type = 'IN_PLACE'` y `table_id` no nulo ocupa exactamente una mesa. No puede haber dos pedidos activos asociados a la misma mesa simultáneamente.

**INV-ORDER-002:** Todos los detalles de un pedido tienen `status = 'PENDING'` al crearse. El `order.status` se deriva de sus detalles (ver SPEC-ORDER-002).

## Criterios de Aceptación

- [ ] **ORD-001:** Pedido válido → `201 Created` + `Order` completo con ID, num, detalles
- [ ] **ORD-002:** Producto inactivo (`is_active = false`) → `400 Bad Request` con mensaje específico
- [ ] **ORD-003:** Producto no disponible (`status = 'OUT_OF_STOCK'`) → `409 Conflict` indicando stock
- [ ] **ORD-004:** Mesa inexistente → `404 Not Found`
- [ ] **ORD-005:** Mesa ocupada (`status = 'OCCUPIED'`) → `409 Conflict` indicando mesa no disponible
- [ ] **ORD-006:** `type = 'IN_PLACE'` sin `tableId` → `400 Bad Request` validación
- [ ] **ORD-007:** `quantity <= 0` en detalle → `400 Bad Request`
- [ ] **ORD-008:** Producto no pertenece al restaurante (tenant violation) → `403 Forbidden`
- [ ] **ORD-009:** Usuario sin rol WAITER/ADMIN → `403 Forbidden`
- [ ] **ORD-010:** Evento `order:created` recibido por clientes conectados del mismo restaurante
- [ ] **ORD-011:** Mesa actualizada a `OCCUPIED` después de crear pedido `IN_PLACE`
- [ ] **ORD-012:** `order.num` es correlativo al último pedido del restaurante (no hay huecos)

## Casos de Borde

1. **Mesa reservada:** Si `table.status = 'RESERVED'`, la spec no especifica comportamiento. Se recomienda `409 Conflict` con indicación de reserva. Requerir decisión de negocio.

2. **Cliente no existe:** Si `clientId` se proporciona pero no existe en DB → `404 Not Found`. Si `clientId` es null, pedido se crea sin cliente.

3. **Producto con opciones:** Si el producto tiene `product_option` obligatorio (según lógica de negocio) y no se proporciona `productOptionId` → `400 Bad Request`. Requerir regla de negocio clara.

4. **Concurrente creación:** Dos pedidos creados simultáneamente para el mismo restaurante → `order.num` debe ser único y sin huecos (ver INV-01). Requiere lock o generación atómica.

## Dependencias

- Tablas: `order`, `order_detail`, `restaurant_table`, `product`, `product_option`, `client`, `app_user`
- RBAC: roles `WAITER`, `ADMIN`
- Tenant context: `x-restaurant-id` header
- WebSocket: STOMP broker con topics por restaurante

## Notas de Implementación

- Use `@Transactional` para garantizar atomicidad de Order + OrderDetails + mesa update.
- El correlativo `num` puede generarse con `SELECT MAX(num) + 1 FROM order WHERE restaurant_id = ?` bajo lock o con secuencia separada si hay problemas de concurrencia.
- El evento WebSocket debe ser emitido después del commit (exitosa transacción).
- Considerar usar MapStruct para mapeo entre DTOs y entidades.
