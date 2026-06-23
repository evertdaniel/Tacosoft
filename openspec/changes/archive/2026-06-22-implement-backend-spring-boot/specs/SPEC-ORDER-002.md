# SPEC-ORDER-002: Cambiar estado de detalle (cocina)

## ID
SPEC-ORDER-002

## Título
Actualización de estado de OrderDetail por cocina

## Actor
`COOK`, `ADMIN`

## Precondiciones
- Usuario autenticado con JWT válido
- Usuario tiene rol `COOK` o `ADMIN` en el restaurante activo
- `OrderDetail` existe y pertenece al restaurante del usuario
- Estado actual del detalle permite la transición deseada

## Flujo

1. Usuario (cocinero) envía `PUT /orders/details/{detailId}/status` con payload:
   ```json
   {
     "status": "IN_PROGRESS"
   }
   ```

2. Backend valida:
   - Usuario tiene rol `COOK` o `ADMIN` (RBAC)
   - `OrderDetail` existe y pertenece al restaurante (tenant filter)
   - Transición de estado es válida según máquina de estados

3. Backend actualiza en transacción:
   - Cambia `order_detail.status` al nuevo valor
   - Recalcula `order.status` basado en todos sus detalles (algoritmo abajo)
   - Actualiza campos derivados si corresponde (ej. `ready_quantity`)

4. Backend emite evento WebSocket:
   - Topic: `/topic/restaurant/{restaurantId}/orders`
   - Evento: `order-detail:updated`
   - Payload: `OrderDetail` actualizado

5. Backend responde con `200 OK`:
   ```json
   {
     "id": "uuid-detail-1",
     "orderId": "uuid-order",
     "productId": "uuid-producto",
     "quantity": 3,
     "status": "IN_PROGRESS",
     "readyQuantity": 0,
     "qtyDelivered": 0,
     "price": 45.00,
     "amount": 135.00,
     "description": "Bien dorados",
     "updatedAt": "2026-06-21T14:35:00Z"
   }
   ```

## Máquina de Estados (OrderDetail)

```
         ┌──────────┐
         │ PENDING  │
         └────┬─────┘
              │
              ▼
      ┌──────────────┐
      │ IN_PROGRESS  │◄───────┐
      └──────┬───────┘        │
             │                │
             ▼                │
      ┌─────────────┐         │
      │    READY    │          │  CANCELLED
      └──────┬──────┘          │  (desde cualquier
             │                 │   estado salvo
             ▼                 │   DELIVERED)
      ┌──────────────┐         │
      │  DELIVERED   │         │
      └──────────────┘         │
                               │
                               └───┘
```

**Transiciones válidas:**
- `PENDING → IN_PROGRESS`
- `IN_PROGRESS → READY`
- `READY → DELIVERED`
- `CANCELLED` (desde PENDING, IN_PROGRESS, o READY) — no desde DELIVERED
- `DELIVERED` es estado final (sin transiciones salientes)

**Transiciones inválidas:**
- `DELIVERED → *` (cualquier estado) — bloquear
- `READY → IN_PROGRESS` (no retroceder)
- `PENDING → READY` (saltarse IN_PROGRESS)

## Recálculo de Order.status

El `order.status` se deriva de sus detalles usando este algoritmo:

```java
OrderStatus deriveOrderStatus(List<OrderDetail> details) {
    boolean anyCancelled = details.stream().anyMatch(d -> d.getStatus() == OrderDetailStatus.CANCELLED);
    boolean allDelivered = details.stream().allMatch(d -> d.getStatus() == OrderDetailStatus.DELIVERED);
    boolean anyInProgress = details.stream().anyMatch(d -> d.getStatus() == OrderDetailStatus.IN_PROGRESS);
    boolean anyReady = details.stream().anyMatch(d -> d.getStatus() == OrderDetailStatus.READY);

    if (allDelivered) return OrderStatus.DELIVERED;
    if (anyInProgress || anyReady) return OrderStatus.IN_PROGRESS;
    if (anyCancelled) return OrderStatus.CANCELLED;
    return OrderStatus.PENDING;  // todos PENDING
}
```

**Estados posibles de Order:**
- `PENDING`: Todos los detalles están PENDING
- `IN_PROGRESS`: Al menos un detalle está IN_PROGRESS o READY
- `DELIVERED`: Todos los detalles están DELIVERED
- `CANCELLED`: Al menos un detail está CANCELLED y ninguno está DELIVERED/IN_PROGRESS/READY

## Postcondiciones
- `OrderDetail.status` actualizado
- `Order.status` recalculado y actualizado
- Evento WebSocket broadcasteado
- No es posible retroceder de DELIVERED

## Invariantes

**INV-ORDER-003:** Un `OrderDetail` en estado `DELIVERED` no puede volver a otro estado. Es un estado final absorbente.

**INV-ORDER-004:** El `Order.status` siempre refleja el estado de sus detalles según el algoritmo de derivación. No se puede establecer manualmente.

**INV-ORDER-005:** Cuando todos los detalles están `DELIVERED`, el `Order.status` es `DELIVERED`. No puede existir un Order con detalles deliverados y status != DELIVERED.

## Criterios de Aceptación

- [ ] **ORD-ST-001:** Transición válida (ej. PENDING→IN_PROGRESS) → `200 OK` + detalle actualizado
- [ ] **ORD-ST-002:** Transición inválida (ej. DELIVERED→IN_PROGRESS) → `409 Conflict` con mensaje de transición no permitida
- [ ] **ORD-ST-003:** Usuario sin rol COOK/ADMIN → `403 Forbidden`
- [ ] **ORD-ST-004:** Detail no existe → `404 Not Found`
- [ ] **ORD-ST-005:** Detail no pertenece al restaurante (tenant) → `403 Forbidden`
- [ ] **ORD-ST-006:** Todos los detalles pasan a DELIVERED → `order.status = 'DELIVERED'`
- [ ] **ORD-ST-007:** Un detalle pasa a IN_PROGRESS → `order.status = 'IN_PROGRESS'`
- [ ] **ORD-ST-008:** Evento `order-detail:updated` broadcasteado a clientes del mismo restaurante
- [ ] **ORD-ST-009:** `updated_at` timestamp actualizado
- [ ] **ORD-ST-010:** CANCEL desde estado válido (PENDING/IN_PROGRESS/READY) → estado CANCELLED
- [ ] **ORD-ST-011:** CANCEL desde DELIVERED → `409 Conflict`

## Casos de Borde

1. **Concurrente update:** Dos cocineros actualizan el mismo detalle simultáneamente → usar versión/optimistic lock o `@Transactional` con serialización. Última transacción gana.

2. **Detalle ya CANCELLED:** Intentar cambiar estado de un detalle ya CANCELLED → permitir solo si el negocio define reactivación (actualmente no especificado; se recomienda bloquear).

3. **Pedido con todos detalles CANCELLED:** Si todos los detalles se cancelan, el Order pasa a `CANCELLED` según algoritmo. El pedido nunca se entregó.

4. **Retroceso manual:** Si un supervisor quiere retroceder de READY a IN_PROGRESS (ej. error de cocina) → transición inválida según máquina actual. Requerir ADR si se permite excepción.

## Dependencias

- Tablas: `order_detail`, `order`, `app_user`, `restaurant`
- RBAC: roles `COOK`, `ADMIN`
- Tenant context: `x-restaurant-id` header
- WebSocket: STOMP broker

## Notas de Implementación

- Validar transiciones con un enum de estados permitidos por cada estado actual.
- Considerar usar Spring State Machine o patrón State si la lógica se complejiza.
- El recálculo de `order.status` debe hacerse en el mismo `@Transactional` que la actualización del detalle.
- Para trazabilidad, considerar guardar `updated_by` (user ID) en el detalle cuando cambia estado (aunque no está en el DDL actual).
