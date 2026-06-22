# SPEC-REPORT-001: Dashboard y reportes

## ID
SPEC-REPORT-001

## Título
Generación de reportes de negocio: dashboard, ventas, productos, finanzas, afluencia, staff

## Actor
`ADMIN`

## Precondiciones
- Usuario autenticado con JWT válido
- Usuario tiene rol `ADMIN` en el restaurante activo

## Tipos de Reportes

### 1. Dashboard (Resumen Ejecutivo)

`GET /reports/dashboard?from=2026-06-01&to=2026-06-30`

Retorna métricas agregadas del período:
```json
{
  "period": { "from": "2026-06-01", "to": "2026-06-30" },
  "sales": {
    "total": 125000.50,
    "count": 850,
    "average": 147.06
  },
  "orders": {
    "total": 850,
    "inPlace": 600,
    "takeAway": 250
  },
  "topProducts": [
    { "productId": "uuid", "name": "Taco de Pastor", "quantity": 420, "revenue": 18900.00 }
  ],
  "topCategories": [
    { "categoryId": "uuid", "name": "Tacos", "revenue": 45000.00 }
  ],
  "occupancy": {
    "peakHour": "14:00",
    "peakDay": "Saturday",
    "averageOccupancy": 0.65
  }
}
```

### 2. Ventas (Sales Report)

`GET /reports/sales?from=2026-06-01&to=2026-06-30&groupBy=day`

Retorna ventas agrupadas por día/semana/mes:
```json
{
  "groupBy": "day",
  "data": [
    {
      "date": "2026-06-01",
      "totalSales": 4500.00,
      "orderCount": 32,
      "averageTicket": 140.63
    }
  ]
}
```

### 3. Productos (Product Performance)

`GET /reports/products?from=2026-06-01&to=2026-06-30&sortBy=quantity`

Retorna ranking de productos:
```json
{
  "products": [
    {
      "productId": "uuid",
      "name": "Taco de Pastor",
      "categoryId": "uuid",
      "categoryName": "Tacos",
      "quantitySold": 420,
      "revenue": 18900.00,
      "cost": 10500.00,
      "profit": 8400.00,
      "margin": 44.4
    }
  ]
}
```

### 4. Finanzas (Financial Report)

`GET /reports/finances?from=2026-06-01&to=2026-06-30`

Retorna movimiento de caja:
```json
{
  "income": {
    "cash": 45000.00,
    "creditCard": 30000.00,
    "transfer": 5000.00,
    "total": 80000.00
  },
  "expenses": {
    "total": 2500.00,
    "byCategory": [
      { "category": "Proveedores", "amount": 2000.00 },
      { "category": "Servicios", "amount": 500.00 }
    ]
  },
  "netCashFlow": 77500.00,
  "cashRegisters": [
    {
      "cashRegisterId": "uuid",
      "openedAt": "2026-06-01T10:00:00Z",
      "closedAt": "2026-06-01T18:00:00Z",
      "openingAmount": 500.00,
      "closingAmount": 8500.00,
      "income": 8000.00
    }
  ]
}
```

### 5. Afluencia (Footfall)

`GET /reports/footfall?from=2026-06-01&to=2026-06-30&groupBy=hour`

Retorna patrones de tráfico:
```json
{
  "groupBy": "hour",
  "data": [
    { "hour": "12:00", "peopleCount": 45, "orderCount": 12 },
    { "hour": "13:00", "peopleCount": 68, "orderCount": 18 }
  ],
  "peak": { "hour": "14:00", "count": 82 }
}
```

### 6. Planificación de Staff (Staff Planning)

`GET /reports/staff-planning?from=2026-06-01&to=2026-06-30`

Retorna recomendaciones de personal:
```json
{
  "recommendations": [
    {
      "day": "Saturday",
      "hour": "14:00",
      "predictedOrders": 25,
      "requiredStaff": { "waiters": 3, "cooks": 2 },
      "currentStaff": { "waiters": 2, "cooks": 2 },
      "suggestion": "Add 1 waiter"
    }
  ]
}
```

## Flujo General

1. Usuario envía `GET /reports/{type}` con query params:
   - `from`: fecha inicio (requerido)
   - `to`: fecha fin (requerido)
   - `groupBy`: agrupación opcional (day, week, month, hour)
   - `sortBy`: ordenamiento (revenue, quantity, profit)

2. Backend valida:
   - Usuario tiene rol `ADMIN`
   - Rango de fechas válido (`from <= to`, no más de 1 año por limitación de performance)
   - `restaurant_id` desde header

3. Backend ejecuta query:
   - Une tablas según tipo de reporte (order, order_detail, invoice, transaction, cash_register)
   - Filtra por `restaurant_id` y rango de fechas
   - Aplica agregaciones (SUM, COUNT, AVG)
   - Ordena y limita si corresponde (top N)

4. Backend retorna `200 OK` con payload estructurado

## Postcondiciones
- Reporte generado con datos correctos y filtrados por tenant
- Performance acceptable (< 3 segundos para rangos ≤ 1 mes)
- Todos los importes financieros provienen de `Transaction` e `Invoice` confirmados (INV-REPORT-001)

## Invariantes

**INV-REPORT-001:** Los importes de reportes financieros provienen exclusivamente de `Transaction` (tipo INCOME/EXPENSE) e `Invoice` con `is_paid = true`. No se incluyen pedidos no cobrados.

**INV-REPORT-002:** Los reportes filtran estrictamente por `restaurant_id`. No hay mezcla de datos entre tenants.

**INV-REPORT-003:** El rango de fechas es inclusivo en ambos extremos: `from <= date <= to`.

**INV-REPORT-004:** Los reportes de producto se basan en `OrderDetail.quantity` no cancelados (status != CANCELLED).

## Criterios de Aceptación

- [ ] **REP-001:** Dashboard con período válido → `200 OK` con métricas correctas
- [ ] **REP-002:** Ventas agrupadas por día → datos correctos + totales coinciden
- [ ] **REP-003:** Productos ordenados por revenue → ranking descendente correcto
- [ ] **REP-004:** Finanzas con ingresos/egresos → `netCashFlow = income - expenses`
- [ ] **REP-005:** Afluencia por hora → peak hour detectado correctamente
- [ ] **REP-006:** Staff planning con recomendaciones → basado en datos históricos
- [ ] **REP-007:** Rango > 1 año → `400 Bad Request` (limitación de performance)
- [ ] **REP-008:** `from > to` → `400 Bad Request`
- [ ] **REP-009:** Usuario sin rol ADMIN → `403 Forbidden`
- [ ] **REP-010:** Reporte con restaurante sin datos → `200 OK` con arrays vacíos (no 404)
- [ ] **REP-011:** Totales cuadran contra datos crudos (test de integridad)

## Casos de Borde

1. **Rango vacío:** Si `from` y `to` son el mismo día y no hay datos → retorna arrays vacíos, no error.

2. **Fechas futuras:** Si `to` es fecha futura → considerar válido (reporte predictivo) o `400 Bad Request`. Requerir decisión de negocio.

3. **Timezone inconsistency:** Si el servidor usa UTC y el cliente usa localtime, ¿cómo se filtra por fecha?
   - Se recomienda que `from/to` se interpreten en timezone del restaurante (configurable)
   - O especificar que siempre es UTC

4. **Grandes rangos:** Si `from - to` = 5 años, ¿se permite?
   - Se recomienda limitar a 1 año para evitar queries pesados
   - Retornar `400 Bad Request` si excede

5. **Productos sin ventas:** En ranking de productos, ¿aparecen con quantity = 0 o se excluyen?
   - Se recomienda excluir (solo top productos con ventas > 0)
   - Pero permitir parámetro `includeZero: true` si se quiere completo

6. **Margen de profit:** Si `unitCost` no está configurado para un producto, ¿cómo se calcula profit?
   - Asumir `cost = 0` y `profit = revenue`
   - O marcar como `null` en el reporte

## Dependencias

- Tablas: `order`, `order_detail`, `invoice`, `transaction`, `cash_register`, `product`, `restaurant_table`
- RBAC: rol `ADMIN`
- Tenant context: `x-restaurant-id` header
- Índices DB: `created_at`, `restaurant_id` en tablas de hechos para performance

## Notas de Implementación

- Usar queries nativas JPQL/Criteria con JOINs optimizados.
- Considerar materialized views o cache para reportes pesados (dashboard, finanzas).
- Para ranking de productos, usar `LIMIT 10` o similar para no retornar miles de filas.
- Considerar paginación (`page`, `size`) para reportes largos.
- Validar que las queries no causen N+1 problems (usar JOIN FETCH).
- Considerar usar StringBuilder dinámico o QueryDSL para filtros opcionales.
- Los reportes no se guardan en DB (se generan on-demand). Considerar cache con TTL si se usan frecuentemente.
- Para staff planning, el algoritmo puede ser simple (promedio histórico) o completo (ML). Iniciar con simple.
