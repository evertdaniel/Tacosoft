# SPEC-MENU-001: Gestión de menú

## ID
SPEC-MENU-001

## Título
CRUD de jerarquía de menú: secciones, categorías, productos y opciones

## Actor
`ADMIN`

## Precondiciones
- Usuario autenticado con JWT válido
- Usuario tiene rol `ADMIN` en el restaurante activo

## Jerarquía del Menú

```
Restaurant
  └─ Section (ej. "Alimentos", "Bebidas")
      └─ Category (ej. "Tacos", "Postres")
          └─ Product (ej. "Taco de Pastor", "Flan")
              └─ ProductOption (ej. "Con todo", "Sin picante")
```

## Flujo CRUD de Section

### Crear Section
1. `POST /sections` con payload:
   ```json
   {
     "name": "Alimentos",
     "displayOrder": 1,
     "isPublic": true,
     "isActive": true
   }
   ```
2. Backend valida nombre único por restaurante (no en DDL, pero recomendado)
3. Persiste con `restaurant_id` desde header
4. Retorna `201 Created` + section completo

### Actualizar Section
1. `PUT /sections/{id}` con campos a modificar
2. Valida que section pertenece al restaurante
3. Retorna `200 OK`

### Eliminar Section
1. `DELETE /sections/{id}`
2. Valida que no tiene categorías hijas ( FK constraint)
3. Si existe, retorna `409 Conflict` indicando dependencias
4. Borra lógico (`isActive = false`) preferido sobre borrado físico

## Flujo CRUD de Category

### Crear Category
1. `POST /categories` con payload:
   ```json
   {
     "sectionId": "uuid-section-alimentos",
     "name": "Tacos",
     "isPublic": true,
     "isActive": true
   }
   ```
2. Valida que `sectionId` existe y pertenece al restaurante
3. Persiste con `restaurant_id` desde header
4. Retorna `201 Created`

### Actualizar/Eliminar Category
- Similar a Section, validando dependencias de productos

## Flujo CRUD de Product

### Crear Product
1. `POST /products` con payload:
   ```json
   {
     "categoryId": "uuid-category-tacos",
     "productionAreaId": "uuid-area-cocina",
     "name": "Taco de Pastor",
     "description": "Cerdo marinado, piña, cilantro",
     "price": 45.00,
     "unitCost": 25.00,
     "iva": 16.00,
     "quantity": 100,
     "status": "AVAILABLE",
     "isActive": true,
     "isPublic": true
   }
   ```
2. Valida que `categoryId` y `productionAreaId` existen y pertenecen al restaurante
3. Valida `price >= 0`, `quantity >= 0`
4. Persiste con `restaurant_id`
5. Retorna `201 Created`

### Actualizar Product
1. `PUT /products/{id}` con campos a modificar
2. Valida pertenencia al restaurante
3. Si cambia `price` a valor negativo → `400 Bad Request`
4. Retorna `200 OK`

### Eliminar Product
1. `DELETE /products/{id}`
2. Valida que no tiene `OrderDetail` asociados (pedidos históricos)
3. Si existe dependencia → borrado lógico (`isActive = false`)
4. Retorna `204 No Content`

## Flujo CRUD de ProductOption

### Crear ProductOption
1. `POST /products/{productId}/options` con payload:
   ```json
   {
     "name": "Con todo",
     "price": 5.00,
     "cost": 2.00,
     "quantity": 50,
     "manageStock": true,
     "isDefault": false,
     "isActive": true,
     "isAvailable": true
   }
   ```
2. Valida que `productId` existe y pertenece al restaurante
3. Persiste ligado al producto
4. Retorna `201 Created`

## Flujo de Importación Excel

1. Usuario envía `POST /products/excel` con file `multipart/form-data`

2. Backend procesa:
   - Parsea Excel (formato esperado: columnas `name`, `category`, `section`, `price`, etc.)
   - Valida cada fila
   - Crea/actualiza productos en batch
   - Retorna resumen de éxitos/errores

3. Retorna `200 OK`:
   ```json
   {
     "totalRows": 150,
     "created": 120,
     "updated": 25,
     "errors": 5,
     "details": [
       { "row": 42, "error": "Categoría 'Postres' no existe" }
     ]
   }
   ```

## Postcondiciones
- Jerarquía consistente: Section → Category → Product → ProductOption
- Todos los items tienen `restaurant_id` del header
- Borrados lógicos se respetan en queries (`isActive = false` excluido de menú público)

## Invariantes

**INV-MENU-001:** Un producto pertenece exactamente a una categoría. No puede haber productos huérfanos o multi-categoría.

**INV-MENU-002:** Una categoría pertenece exactamente a una sección.

**INV-MENU-003:** `price >= 0` y `quantity >= 0` para productos y opciones.

**INV-MENU-004:** Un producto con `OrderDetail` históricos no puede eliminarse físicamente. Solo borrado lógico (`isActive = false`).

**INV-MENU-005:** Si `manageStock = true` y `quantity = 0`, el producto no está disponible (`status = OUT_OF_STOCK` por lógica de negocio, no por DB).

## Criterios de Aceptación

- [ ] **MENU-001:** Crear section válida → `201 Created`
- [ ] **MENU-002:** Crear categoría con section inexistente → `404 Not Found`
- [ ] **MENU-003:** Crear producto con price negativo → `400 Bad Request`
- [ ] **MENU-004:** Borrar producto con pedidos históricos → borrado lógico (`isActive = false`) + `200 OK`
- [ ] **MENU-005:** Borrar producto sin dependencias → borrado físico + `204 No Content`
- [ ] **MENU-006:** Importar Excel válido → batch create/update + resumen
- [ ] **MENU-007:** Importar Excel con errores → partial success + detalle de errores
- [ ] **MENU-008:** Usuario sin rol ADMIN → `403 Forbidden`
- [ ] **MENU-009:** Item no pertenece al restaurante (tenant) → `403 Forbidden`
- [ ] **MENU-010:** GET /menú público retorna solo `isPublic=true` + `isActive=true`
- [ ] **MENU-011:** Actualizar producto agrega `updated_at` timestamp

## Casos de Borde

1. **Reordenamiento:** Si cambia `displayOrder` de secciones/categorías, el frontend debe respetar el orden. Backend no valida unicidad; puede haber duplicados.

2. **Stock management:** Si `manageStock = true` y `quantity = 0`, ¿se bloquea creación de OrderDetail con este producto? La spec actual no lo especifica. Se recomienda:
   - Validad en `OrderService` al crear pedido
   - Retornar `409 Conflict` indicando sin stock

3. **Cambio de precio:** Si se cambia el `price` de un producto, ¿afecta pedidos existentes? No, los `OrderDetail` guardan el `price` al momento del pedido (snapshot).

4. **Borrado en cascada:** Si se borra una Section con Categories, ¿qué pasa?
   - FK constraint evita borrado físico
   - Requerir borrado lógico en cascada manual
   - O implementar borrado en cascada lógico (actualizar `isActive` en dependientes)

5. **Producto sin producción área:** Si `productionAreaId` es null, ¿se puede crear? DDL permite null. Requerir regla de negocio: ¿es obligatorio?

## Dependencias

- Tablas: `section`, `category`, `product`, `product_option`, `production_area`, `restaurant`
- RBAC: rol `ADMIN`
- Tenant context: `x-restaurant-id` header
- Excel library (Apache POI o similar)

## Notas de Implementación

- Considerar usar `@OneToMany(cascade = CascadeType.ALL)` solo para borrado físico; borrado lógico requiere lógica custom.
- Para importación Excel, validar tamaño de file (max ~5MB) para evitar OOM.
- El menú público (`GET /menu`) debe filtrar por `isPublic=true`, `isActive=true` y tenant.
- Considerar cache del menú público (Redis) ya que cambia poco.
- Para `displayOrder`, usar `ORDER BY display_order ASC` en queries.
