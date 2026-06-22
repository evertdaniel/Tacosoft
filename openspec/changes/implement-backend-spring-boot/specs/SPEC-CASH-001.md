# SPEC-CASH-001: Apertura y cierre de caja (X/Z)

## ID
SPEC-CASH-001

## Título
Gestión de sesiones de caja y reportes X/Z

## Actor
`CASHIER`, `ADMIN`

## Precondiciones
- Usuario autenticado con JWT válido
- Usuario tiene rol `CASHIER` o `ADMIN` en el restaurante activo

## Flujo de Apertura

1. Usuario envía `POST /cash-registers` con payload:
   ```json
   {
     "openingAmount": 500.00
   }
   ```

2. Backend valida:
   - Usuario tiene rol `CASHIER` o `ADMIN`
   - No existe otra caja abierta para el mismo restaurante (según invariante INV-CASH-001)

3. Backend persiste:
   - Crea `CashRegister` con:
     - `restaurant_id` desde header
     - `user_id` desde JWT `sub` claim
     - `opening_amount` especificado
     - `status = 'OPEN'`
     - `opened_at = NOW()`

4. Backend responde con `201 Created`:
   ```json
   {
     "id": "uuid-caja",
     "restaurantId": "uuid-restaurante",
     "userId": "uuid-cajero",
     "openingAmount": 500.00,
     "status": "OPEN",
     "openedAt": "2026-06-21T10:00:00Z"
   }
   ```

## Flujo de Reporte X (Corte Parcial)

1. Usuario envía `GET /cash-registers/active` con header `x-restaurant-id`

2. Backend retorna la caja abierta:
   ```json
   {
     "id": "uuid-caja",
     "openingAmount": 500.00,
     "status": "OPEN",
     "openedAt": "2026-06-21T10:00:00Z",
     "transactions": [
       {
         "id": "uuid-tx-1",
         "type": "INCOME",
         "amount": 156.60,
         "paymentMethod": "CASH",
         "description": "Invoice #10042",
         "createdAt": "2026-06-21T12:30:00Z"
       }
     ],
     "summary": {
       "totalIncome": 5420.50,
       "totalExpense": 150.00,
       "expectedCash": 5970.50
     }
   }
   ```
   - **NOTA:** Este no cierra la caja; es solo informativo (corte X).

## Flujo de Cierre (Reporte Z)

1. Usuario envía `PUT /cash-registers/{cashRegisterId}/close` con payload:
   ```json
   {
     "closingAmount": 5970.50
   }
   ```

2. Backend valida:
   - Caja existe y pertenece al restaurante
   - Caja está abierta (`status = 'OPEN'`)
   - Usuario es el mismo que la abrió (o es ADMIN)

3. Backend calcula diferencias:
   ```java
   BigDecimal expected = openingAmount + totalIncome - totalExpense;
   BigDecimal difference = closingAmount - expected;
   ```

4. Backend persiste en transacción:
   - Actualiza `CashRegister`:
     - `closing_amount = 5970.50`
     - `status = 'CLOSED'`
     - `closed_at = NOW()`
   - Registra discrepancia si existe (no especificado en DDL; podría ser un transaction de tipo ajuste)

5. Backend genera reporte Z:
   ```json
   {
     "cashRegisterId": "uuid-caja",
     "openingAmount": 500.00,
     "totalIncome": 5420.50,
     "totalExpense": 150.00,
     "expectedCash": 5970.50,
     "declaredCash": 5970.50,
     "difference": 0.00,
     "openedAt": "2026-06-21T10:00:00Z",
     "closedAt": "2026-06-21T18:00:00Z",
     "transactionCount": 42,
     "transactions": [
       // lista completa de transactions
     ]
   }
   ```

6. Backend responde con `200 OK` + reporte Z

## Postcondiciones
- Caja marcada como `CLOSED`
- `closed_at` timestamp registrado
- No se pueden agregar más transactions a esta caja
- Reporte Z generado con totales cuadrados

## Invariantes

**INV-CASH-001:** Por defecto, **una caja abierta por restaurante**. No puede haber dos `CashRegister` con `status = 'OPEN'` para el mismo `restaurant_id`.

   *NOTA:* Si el modelo evoluciona a "una caja abierta por user_id" (multi-cajero), esta invariante debe versionarse explícitamente (INV-CASH-001 v2) con aprobación ADR y juicio doble.

**INV-CASH-002:** `saldo_final = saldo_inicial + Σ(ingresos) − Σ(gastos)`. El reporte Z debe cuadrar contra las transactions.

**INV-05:** Una caja cerrada no admite nuevas transactions. Cualquier intento de crear `Transaction` con `cash_register_id` de una caja cerrada debe retornar `409 Conflict`.

**INV-CASH-003:** Solo el usuario que abrió la caja (o un ADMIN) puede cerrarla. Previene cierres no autorizados.

## Criterios de Aceptación

- [ ] **CASH-001:** Abrir caja válida → `201 Created` con estado OPEN
- [ ] **CASH-002:** Abrir caja con otra ya abierta (mismo restaurante) → `409 Conflict` (INV-CASH-001)
- [ ] **CASH-003:** Cerrar caja válida → `200 OK` + reporte Z
- [ ] **CASH-004:** Reporte Z cuadra contra transactions (INV-CASH-002)
- [ ] **CASH-005:** Caja cerrada no acepta nuevas transactions → `409 Conflict` (INV-05)
- [ ] **CASH-006:** Usuario distinto al que abrió intenta cerrar → `403 Forbidden` (salvo ADMIN)
- [ ] **CASH-007:** Cerrar caja que ya está cerrada → `409 Conflict`
- [ ] **CASH-008:** Reporte X (GET active) retorna resumen sin cerrar
- [ ] **CASH-009:** Diferencia en arqueo se registra en reporte Z (si `closingAmount != expected`)
- [ ] **CASH-010:** Usuario sin rol CASHIER/ADMIN → `403 Forbidden`

## Casos de Borde

1. **Arqueo con diferencia:** Si `closingAmount != expected`, la spec no especifica acciones correctivas. Se recomienda:
   - Registrar la diferencia en el reporte Z
   - Considerar crear un `Transaction` de ajuste (type = 'ADJUSTMENT') no especificado en DDL
   - Requerir aprobación de supervisor para diferencias > threshold

2. **Caja abierta de sesión anterior:** Si el servidor se reinicia con una caja abierta (no cerró properly), ¿cómo se recupera?
   - Permitir "forzar cierre" con rol ADMIN
   - O marcar como cerrada automáticamente al detectar fecha anterior
   - Requerir decisión de negocio

3. **Multi-cajero:** Si el negocio evoluciona a múltiples cajas abiertas simultáneas (una por cajero), romper INV-CASH-001. Requiere:
   - ADR que supere INV-CASH-001
   - INV-CASH-001 v2: "una caja abierta por user_id por restaurante"
   - Juicio doble por implicaciones financieras
   - Actualizar RBAC y tests

4. **Caja sin transactions:** Si se cierra una caja sin ningún movimiento → reporte Z con `totalIncome = 0`, `totalExpense = 0`, `expected = openingAmount`.

5. **Reapertura el mismo día:** ¿Puede el mismo cajero cerrar y abrir otra caja el mismo día? La spec actual lo permite (no hay restricción temporal). Considerar si requiere limitación.

## Dependencias

- Tablas: `cash_register`, `transaction`, `app_user`, `restaurant`
- RBAC: roles `CASHIER`, `ADMIN`
- Tenant context: `x-restaurant-id` header
- **💰 JUICIO DOBLE** (ADR-005): Todo PR que toque estas tablas requiere doble revisión adversarial

## Notas de Implementación

- Para INV-CASH-001, usar una query `SELECT COUNT(*) FROM cash_register WHERE restaurant_id = ? AND status = 'OPEN'` antes de insertar, o una unique constraint parcial `(restaurant_id, status) WHERE status = 'OPEN'`.
- El reporte Z se genera agregando las `Transaction` ligadas a la caja; no se guarda en DB (es solo response).
- Considerar guardar el reporte Z en una tabla `cash_register_report` para auditoría histórica (no en DDL actual).
- Si se implementa diferencias con `Transaction` de ajuste, asegurar que no afecte INV-CASH-002 (el ajuste debe ser parte de Σ(gastos)).
- Para prevenir concurrencia en cierre, usar `@Transactional` y verificar que la caja siga OPEN antes de actualizar.
