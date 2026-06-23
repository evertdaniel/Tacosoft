# SPEC-BILL-001: Crear y cobrar cuenta

## ID
SPEC-BILL-001

## Título
Generación de bill/invoice y procesamiento de pagos

## Actor
`CASHIER`, `ADMIN`

## Precondiciones
- Usuario autenticado con JWT válido
- Usuario tiene rol `CASHIER` o `ADMIN` en el restaurante activo
- Order existe y no está completamente pagado
- Caja del usuario está abierta (`cash_register.status = 'OPEN'`)

## Flujo de Creación

1. Usuario envía `POST /invoices` con header `x-restaurant-id` y payload:
   ```json
   {
     "orderId": "uuid-order",
     "paymentMethod": "CASH"  // CASH | CREDIT_CARD | TRANSFER
   }
   ```

2. Backend valida:
   - Usuario tiene rol `CASHIER` o `ADMIN`
   - Order existe y pertenece al restaurante
   - Order no está completamente pagado (`is_paid = false` o existen bills impagos)
   - Caja del usuario está abierta

3. Backend asigna folio (INV-02):
   - Bloquea fila `folio_sequence` para el restaurante (`SELECT ... FOR UPDATE`)
   - Lee `next_folio`
   - Asigna folio al invoice
   - Incrementa `next_folio` + 1
   - Libera lock (commit)

4. Backend calcula totales:
   - `subtotal = Σ(order_detail.amount where detail no cancelado)`
   - `tax = subtotal * iva_rate` (según configuración del restaurante o productos)
   - `total = subtotal + tax`

5. Backend persiste en transacción:
   - Crea `Invoice` con folio asignado y totales
   - Crea `InvoiceDetail` por cada `OrderDetail` no cancelado

6. Backend responde con `201 Created`:
   ```json
   {
     "id": "uuid-invoice",
     "orderId": "uuid-order",
     "folio": 10042,
     "subtotal": 135.00,
     "tax": 21.60,
     "total": 156.60,
     "isPaid": false,
     "paymentMethod": "CASH",
     "createdAt": "2026-06-21T15:00:00Z"
   }
   ```

## Flujo de Cobro

1. Usuario envía `POST /invoices/{invoiceId}/pay` con payload:
   ```json
   {
     "paymentMethod": "CASH",
     "amount": 156.60,
     "referenceId": "uuid-invoice"  // idempotencia
   }
   ```

2. Backend valida:
   - Invoice existe y pertenece al restaurante
   - Invoice no está pagado (`is_paid = false`)
   - Caja del usuario está abierta
   - `amount` coincide con `invoice.total` (para pago completo) o es menor (pago parcial)

3. Backend verifica idempotencia (INV-03):
   - Busca `transaction` con `reference_id = invoice.id`
   - Si existe → ya cobrado, retornar `200 OK` sin duplicar transacción

4. Backend persiste en transacción:
   - Crea `Transaction` de tipo INCOME:
     - `cash_register_id`: caja abierta del usuario
     - `amount`: monto del cobro
     - `payment_method`: especificado en request
     - `reference_id`: invoice ID (para idempotencia)
   - Marca `invoice.is_paid = true` si el pago cubre el total
   - Si el invoice es el último del order, marca `order.is_paid = true`

5. Backend responde con `200 OK`:
   ```json
   {
     "id": "uuid-invoice",
     "folio": 10042,
     "total": 156.60,
     "isPaid": true,
     "paymentMethod": "CASH",
     "transactionId": "uuid-transaction"
   }
   ```

## Postcondiciones (Cobro)
- `Transaction` creada y ligada a la caja
- `Invoice.is_paid = true` (pago completo)
- `Order.is_paid = true` si todos sus invoices están pagos
- El folio asignado no se reutiliza nunca

## Invariantes

**INV-02 (Folio):** El folio es único por restaurante. La secuencia `folio_sequence.next_folio` es la única fuente de verdad. Bajo alta concurrencia, el lock pesimista garantiza contigüidad sin huecos.

**INV-03 (Idempotencia):** No se puede cobrar dos veces el mismo invoice. La constraint `UNIQUE (reference_id)` en `transaction` previene duplicados. Un reintentó de pago debe detectar la transacción existente y retornar éxito sin duplicar.

**INV-BILL-001:** `Σ(invoice.total) ≤ order.total` para todas las invoices de un order. No se puede facturar más de lo que vale el pedido (salvo ajustes manuales no especificados).

**INV-BILL-002:** Un invoice no puede crearse si ya existe otro invoice con el mismo folio para el mismo restaurante.

**INV-BILL-003:** No se puede cobrar un invoice si la caja del usuario está cerrada. La transacción debe estar ligada a una caja abierta.

## Criterios de Aceptación

- [ ] **BILL-001:** Crear invoice válido → `201 Created` con folio único
- [ ] **BILL-002:** Asignación de folio concurrente (100 invoices simultáneos) → folios contiguos sin huecos ni duplicados (INV-02)
- [ ] **BILL-003:** Pago completo → `invoice.is_paid = true` + `transaction` de INCOME creada
- [ ] **BILL-004:** Reintentar mismo pago → no duplica transacción, retorna `200 OK` (INV-03)
- [ ] **BILL-005:** Cobro sin caja abierta → `409 Conflict` indicando caja cerrada
- [ ] **BILL-006:** Pago de invoice ya pagado → retorna `409 Conflict` o ignora si es idempotente
- [ ] **BILL-007:** `invoice.total = subtotal + tax` correctamente calculado
- [ ] **BILL-008:** `order.is_paid = true` cuando todos sus invoices están pagos
- [ ] **BILL-009:** Folio nunca se repite para el mismo restaurante (test de unicidad)
- [ ] **BILL-010:** Usuario sin rol CASHIER/ADMIN → `403 Forbidden`

## Casos de Borde

1. **Pago parcial:** El request permite `amount < invoice.total`. La spec no especifica comportamiento. Se recomienda:
   - Permitir pago parcial → `invoice.is_paid = false` hasta completar
   - Crear múltiples `transaction` con mismo `reference_id` no permitido (constraint unique)
   - Requerir decisión de negocio: ¿se permiten pagos parciales? ¿Cómo se trackean?

2. **Concurrente pago del mismo invoice:** Dos requests simultáneos para pagar el mismo invoice → la constraint `UNIQUE (reference_id)` debe causar que uno falle con `409 Conflict` o se maneje con idempotencia (uno inserta, el otro detecta y retorna éxito).

3. **Order con descuento:** Si el order tiene descuento (no en DDL actual), el `invoice.total` debe reflejarlo. Requerir lógica de negocio si se implementa.

4. **Cambio de medio de pago:** Si se crea invoice con `paymentMethod = 'CASH'` pero luego se paga con tarjeta → el `invoice.payment_method` se actualiza al cobrar, o se rechaza. Requerir regla.

5. **Invoice con amount > order.total:** Validación debe rechazar con `400 Bad Request` para evitar sobrefacturación (INV-BILL-001).

## Dependencias

- Tablas: `invoice`, `invoice_detail`, `transaction`, `cash_register`, `order`, `folio_sequence`
- RBAC: roles `CASHIER`, `ADMIN`
- Tenant context: `x-restaurant-id` header
- **💰 JUICIO DOBLE** (ADR-005): Todo PR que toque estas tablas requiere doble revisión adversarial

## Notas de Implementación

- Usar `@Transactional` con aislamiento `SERIALIZABLE` o `REPEATABLE_READ` para evitar race conditions en folio.
- El lock `SELECT ... FOR UPDATE` en `folio_sequence` debe mantenerse por el mínimo tiempo posible.
- Para INV-03, considerar usar `INSERT IGNORE` o `ON CONFLICT DO NOTHING` si la DB lo soporta, o validar antes de insert.
- El `reference_id` en `transaction` es la clave de idempotencia. Usar el invoice ID es simple y efectivo.
- Considerar crear una secuencia por restaurante si `folio_sequence` se convierte en cuello de botella (actualmente no anticipado).
