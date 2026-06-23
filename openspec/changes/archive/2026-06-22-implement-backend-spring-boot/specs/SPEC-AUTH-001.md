# SPEC-AUTH-001: Inicio de sesión

## ID
SPEC-AUTH-001

## Título
Inicio de sesión con JWT

## Actor
Cualquier usuario registrado en el sistema.

## Precondiciones
- Usuario existe en la base de datos con credenciales válidas
- Usuario tiene estado `is_active = true`
- Usuario tiene al menos un rol asignado en algún restaurante

## Flujo

1. Usuario envía `POST /auth/login` con payload:
   ```json
   {
     "username": "mesero01",
     "password": "claveSegura123"
   }
   ```

2. Backend valida credenciales:
   - Busca usuario por `username` en `app_user`
   - Compara hash BCrypt con `password` proporcionado
   - Verifica que `is_active = true`

3. Si credenciales válidas, backend genera JWT con claims:
   - `sub`: user ID (UUID)
   - `username`: nombre de usuario
   - `role`: rol primario (ADMIN, COOK, WAITER, CASHIER)
   - `restaurantRoles`: array de objetos `{ restaurantId, role }`
   - `exp`: timestamp de expiración (120 minutos por defecto)
   - `iat`: issued at timestamp

4. Backend responde con `200 OK`:
   ```json
   {
     "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     "user": {
       "id": "uuid-del-usuario",
       "username": "mesero01",
       "role": { "id": 3, "name": "WAITER" },
       "restaurantRoles": [
         { "restaurantId": "uuid-restaurante-1", "role": "WAITER" },
         { "restaurantId": "uuid-restaurante-2", "role": "ADMIN" }
       ]
     },
     "currentRestaurant": {
       "id": "uuid-restaurante-1",
       "name": "El Buen Taco"
     }  // null si usuario no tiene restaurantes
   }
   ```

## Postcondiciones
- Cliente almacena token en localStorage
- Token se incluye en header `Authorization: Bearer <token>` en subsiguientes requests
- Estado de Redux pasa a `authenticated`
- Si existe `currentRestaurant`, se establece como restaurante activo

## Invariantes

**INV-AUTH-001:** Un token expirado nunca autoriza una request. El filtro JWT debe rechazar con `401 Unauthorized` antes de到达 cualquier controller.

**INV-AUTH-002:** Credenciales inválidas (username no existe o password incorrecto) retornan `401 Unauthorized` sin revelar si el usuario existe (evitar enumeración de usuarios).

**INV-AUTH-003:** Usuario con `is_active = false` retorna `401 Unauthorized` con mensaje genérico.

## Criterios de Aceptación

- [ ] **AUT-001:** Credenciales válidas → `200 OK` + token JWT firmado correctamente
- [ ] **AUT-002:** Username incorrecto → `401 Unauthorized` sin leak de existencia
- [ ] **AUT-003:** Password incorrecto → `401 Unauthorized` sin leak de existencia
- [ ] **AUT-004:** Usuario inactivo (`is_active = false`) → `401 Unauthorized`
- [ ] **AUT-005:** Usuario sin restaurantes asignados → `currentRestaurant: null` en respuesta
- [ ] **AUT-006:** Token decodificado contiene claims `sub`, `role`, `restaurantRoles`, `exp`, `iat`
- [ ] **AUT-007:** Token expira después del tiempo configurado (defecto: 120 minutos)
- [ ] **AUT-008:** Token con firma inválida → `401 Unauthorized`
- [ ] **AUT-009:** Payload mal formado → `400 Bad Request`

## Casos de Borde

1. **Usuario con múltiples restaurantes:** `restaurantRoles` contiene todos los roles del usuario; `currentRestaurant` se establece al primero o null según lógica de negocio del frontend.

2. **Token expirado durante request:** El filtro JWT debe detectar `exp < now` y retornar `401` con header `WWW-Authenticate: Bearer error="invalid_token"`.

3. **Concurrente login desde múltiples dispositivos:** Cada dispositivo obtiene su propio token; todos son válidos hasta expiración (no se invalidan tokens previos a menos que se implemente revocación).

## Dependencias

- Tablas: `app_user`, `role`, `user_restaurant_role`, `restaurant`
- BCrypt password hashing (cost ≥ 10)
- JWT library (JJWT o similar)
- Spring Security filter chain

## Notas de Implementación

- El password nunca debe retornarse en ninguna response.
- El JWT secret debe ser configurado por variable de entorno `JWT_SECRET`.
- Considerar implementar refresh token (actualmente no en scope).
- Para BCrypt, usar `BCryptPasswordEncoder` de Spring Security con defecto strength 10.
