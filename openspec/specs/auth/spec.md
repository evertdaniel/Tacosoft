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
       "name": "Restaurante Central"
     }
   }
   ```

5. Cliente almacena el token y lo envía en header `Authorization: Bearer <token>` en requests subsiguientes.

## Postcondiciones
- Usuario autenticado recibe token JWT válido
- Token contiene roles del usuario en todos sus restaurantes
- Cliente puede usar el token para acceder a endpoints protegidos

## Invariants
- **INV-AUTH-001**: Tokens deben expirar en máximo 120 minutos
- **INV-AUTH-002**: Tokens deben ser firmados con HS256 y secret de al menos 256 bits

## Aceptación
1. Login exitoso con credenciales válidas retorna 200 y token
2. Login fallido con credenciales inválidas retorna 401
3. Login con usuario inactivo retorna 401
4. Token contiene todos los roles del usuario
5. Token expira después del tiempo configurado
6. Refresh de token (si se implementa) requiere token válido

## Casos borde
- Usuario no existe: 401
- Password incorrecto: 401
- Usuario inactivo: 401
- Usuario sin roles: 403
- Request sin campos requeridos: 400

## Dependencias
- Base de datos con tablas `app_user`, `role`, `user_restaurant_role`
- Configuración de JWT (secret, expiration)

## Notas de implementación
- Usar BCrypt para hash de passwords
- Usar JJWT o similar para generar/validar tokens
- Token debe incluir información de multi-tenancy (restaurantRoles)
- Considerar refresh tokens para UX mejorada
