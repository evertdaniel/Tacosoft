# SPEC-TABLE-001: Gestión de mesas

## ID
SPEC-TABLE-001

## Título
CRUD de mesas y gestión de estados para el layout del restaurante

## Actor
`ADMIN`, `WAITER`

## Precondiciones
- Usuario autenticado con JWT válido
- Usuario tiene rol `ADMIN` o `WAITER` en el restaurante activo

## Flujo CRUD de Mesa

### Crear Mesa (solo ADMIN)
1. `POST /tables` con payload:
   ```json
   {
     "name": "Mesa 12",
     "seats": 4,
     "posX": 150,
     "posY": 200,
     "isActive": true
   }
   ```
2. Valida que `name` es único por restaurante (recomendado, no en DDL)
3. Valida `seats >= 1`, `posX >= 0`, `posY >= 0`
4. Persiste con `restaurant_id` desde header
5. Retorna `201 Created` + mesa completa

### Actualizar Mesa
1. `PUT /tables/{id}` con campos a modificar:
   ```json
   {
     "name": "Mesa 12-A",
     "seats": 6,
     "posX": 180,
     "posY": 220
   }
   ```
2. Valida que mesa existe y pertenece al restaurante
3. Valida restricciones de positividad
4. Actualiza + `updated_at` timestamp
5. Retorna `200 OK`

### Eliminar Mesa (solo ADMIN)
1. `DELETE /tables/{id}`
2. Valida que mesa no tiene pedidos activos (`order` con `table_id` y `is_closed = false`)
3. Si tiene pedidos activos → `409 Conflict`
4. Si solo tiene pedidos históricos → borrado lógico (`isActive = false`)
5. Retorna `204 No Content`

## Flujo de Cambio de Estado

### Actualizar Estado de Mesa
1. `PUT /tables/{id}/status` con payload:
   ```json
   {
     "status": "OCCUPIED"
   }
   ```

2. Estados válidos: `AVAILABLE`, `OCCUPIED`, `RESERVED`, `CLEANING`

3. Valida transiciones según lógica de negocio:
   - `AVAILABLE → OCCUPIED`: permitido (cuando se crea pedido)
   - `OCCUPIED → AVAILABLE`: permitido (cuando pedido se cierra/mesa se libera)
   - `OCCUPIED → CLEANING`: permitido (comienza limpieza)
   - `CLEANING → AVAILABLE`: permitido (limpieza terminada)
   - `AVAILABLE → RESERVED`: permitido (reserva futura)
   - `RESERVED → OCCUPIED`: permitido (cliente llega)
   - `RESERVED → AVAILABLE`: permitido (reserva cancelada)

4. Persiste cambio de estado + actualiza `updated_at`

5. Emite evento WebSocket:
   - Topic: `/topic/restaurant/{restaurantId}/tables`
   - Evento: `table:updated`
   - Payload: mesa completa

6. Retorna `200 OK` + mesa actualizada

## Flujo de Layout (Drag-and-Drop)

### Actualizar Posición
1. `PUT /tables/{id}/position` con payload:
   ```json
   {
     "posX": 250,
     "posY": 300
   }
   ```

2. Valida que mesa existe y pertenece al restaurante
3. Actualiza coordenadas + `updated_at`
4. Emite evento `table:updated`
5. Retorna `200 OK`

## Postcondiciones
- Mesa persistida o actualizada con estado correcto
- Layout refleja nuevas coordenadas
- Evento WebSocket broadcasteado a clientes del restaurante

## Invariantes

**INV-TABLE-001:** Una mesa con `status = 'OCCUPIED'` tiene a lo sumo un pedido activo asociado (`order` con `table_id` y `is_closed = false`). No puede haber múltiples pedidos simultáneos para la misma mesa.

**INV-TABLE-002:** `seats >= 1`. No existen mesas con capacidad cero o negativa.

**INV-TABLE-003:** `posX >= 0` y `posY >= 0`. Coordenadas negativas no permitidas en el layout.

**INV-TABLE-004:** No se puede eliminar una mesa con pedidos activos. El borrado físico falla con `409 Conflict`.

## Criterios de Aceptación

- [ ] **TABLE-001:** Crear mesa válida → `201 Created` (solo ADMIN)
- [ ] **TABLE-002:** Crear mesa con seats <= 0 → `400 Bad Request`
- [ ] **TABLE-003:** Crear mesa por WAITER → `403 Forbidden` (solo ADMIN)
- [ ] **TABLE-004:** Actualizar estado válido (AVAILABLE→OCCUPIED) → `200 OK` + evento WebSocket
- [ ] **TABLE-005:** Actualizar estado inválido → `400 Bad Request` o `409 Conflict`
- [ ] **TABLE-006:** Actualizar posición (drag-and-drop) → `200 OK` + evento `table:updated`
- [ ] **TABLE-007:** Eliminar mesa con pedidos activos → `409 Conflict`
- [ ] **TABLE-008:** Eliminar mesa sin pedidos activos → borrado lógico + `204 No Content`
- [ ] **TABLE-009:** GET /tables retorna mesas ordenadas por nombre
- [ ] **TABLE-010:** Usuario sin rol ADMIN/WAITER → `403 Forbidden`
- [ ] **TABLE-011:** Mesa con pedido activo pasa a AVAILABLE solo si el pedido se cerró

## Casos de Borde

1. **Mesa ocupada con múltiples pedidos:** Si por bug se crean dos pedidos para la misma mesa (violación de INV-TABLE-001), ¿qué pasa?
   - Validar en `OrderService` antes de crear pedido
   - Requerir lógica de recuperación: ¿cuál pedido prevalece?

2. **Mesa en estado CLEANING:** ¿Puede asignarse a un nuevo pedido?
   - Se recomienda bloquear: `CLEANING` no permite `OCCUPIED`
   - O permitirla si el negocio lo requiere

3. **Reserva overshoot:** Si `status = 'RESERVED'` y la reserva pasa, ¿quién la actualiza?
   - Requerir job programado o actualización manual
   - No especificado en spec actual

4. **Coordenadas de layout:** ¿Hay límites máximos para `posX`, `posY`?
   - Se recomienda validar rango razonable (0-10000) para evitar desbordamiento
   - No especificado en DDL

5. **Mesa con seats = 0:** Si se actualiza `seats` a 0, ¿se permite?
   - Según INV-TABLE-002, no. Validar en backend.

6. **Borrado de mesa histórica:** Si se elimina una mesa con pedidos históricos (cerrados), ¿qué pasa con los pedidos?
   - `order.table_id` se vuelve null (borrado lógico) o se rompe FK
   - Requerir decisión: ¿se mantiene la referencia o se limpia?

## Dependencias

- Tablas: `restaurant_table`, `order`, `restaurant`
- RBAC: roles `ADMIN`, `WAITER`
- Tenant context: `x-restaurant-id` header
- WebSocket: STOMP broker

## Notas de Implementación

- Para INV-TABLE-001, validar en `OrderService` antes de crear pedido: `SELECT COUNT(*) FROM order WHERE table_id = ? AND is_closed = false`. Si > 0 → `409 Conflict`.
- Considerar usar una enum de estados con transiciones válidas (similar a OrderDetail).
- El layout (posX, posY) es puramente visual; no afecta lógica de negocio.
- Para borrado lógico, actualizar `isActive = false` y mantener registros históricos.
- Considerar añadir `capacity` (diferente a `seats`) si se manejan eventos con más gente que sillas.
- El evento WebSocket `table:updated` debe incluir la mesa completa para que el frontend actualice el layout sin requery.
