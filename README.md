# Lumina Home – Backend

API REST de la tienda Lumina Home, hecha con Spring Boot 4 y MongoDB Atlas. La consume el frontend en React ([mi-app-react](https://github.com/AbrahJosee/Evaluacion-1-Web)).

## Requisitos

- JDK 17 o superior
- Una base de MongoDB Atlas (o MongoDB local)
- Maven no hace falta: el proyecto trae Maven Wrapper (`mvnw.cmd`)

## Configuración

Las credenciales no van en el repositorio. Copia `.env.example` como `.env` en la raíz del proyecto y completa:

```properties
MONGODB_URI=mongodb+srv://<usuario>:<password>@<cluster>.mongodb.net/lumina?retryWrites=true&w=majority
JWT_SECRET=<al menos 32 caracteres al azar>
```

Spring lee ese archivo con `spring.config.import=optional:file:.env[.properties]` (en `application.properties`). El `.env` está en `.gitignore`.

## Ejecutar

```powershell
.\mvnw.cmd spring-boot:run
```

La API queda en http://localhost:8081. La primera vez que arranca, `CargaDatos` llena las colecciones vacías con los JSON de `src/main/resources/data` (categorías, productos y usuarios, con las contraseñas encriptadas con BCrypt).

Usuarios de prueba:

| Perfil | Correo | Contraseña |
| --- | --- | --- |
| administrador | ab.herreraa@duoc.cl | admin123 |
| vendedor | matias.reyes@luminahome.cl | vendedor123 |
| cliente | camila.ibanez@gmail.com | cliente123 |

## Swagger

Con el backend corriendo, abre http://localhost:8081/swagger-ui.html.

Para probar los endpoints protegidos:
1. Ejecuta `POST /api/auth/login` con un usuario de prueba.
2. Copia el `accessToken` de la respuesta.
3. Pulsa **Authorize** (arriba a la derecha), pega el token y confirma.

## Estructura

```
cl.duoc.lumina
├── model         Documentos de MongoDB: Producto, Categoria, Usuario, Orden
├── repository    Interfaces MongoRepository (Spring Data arma las consultas)
├── service       Lógica de negocio y validaciones
├── controller    Endpoints REST documentados con anotaciones de Swagger
├── security      JWT: generación y verificación de tokens, filtro y reglas por rol
└── config        Carga inicial de datos
```

## Endpoints

| Método | Ruta | Acceso |
| --- | --- | --- |
| POST | `/api/auth/login` | Público |
| POST | `/api/auth/registro` | Público (crea clientes) |
| POST | `/api/auth/refresh` | Público (requiere refresh token) |
| POST | `/api/auth/logout` | Con sesión |
| GET | `/api/auth/yo` | Con sesión |
| GET | `/api/productos`, `/api/productos/{codigo}` | Público |
| POST, PUT | `/api/productos/**` | Administrador, vendedor |
| DELETE | `/api/productos/{codigo}` | Administrador |
| GET | `/api/categorias` | Público |
| POST, PUT, DELETE | `/api/categorias/**` | Administrador |
| GET, POST, PUT, DELETE | `/api/usuarios/**` | Administrador |
| POST | `/api/ordenes` | Con sesión |
| GET | `/api/ordenes/mias` | Con sesión |
| GET | `/api/ordenes` | Administrador, vendedor |
| PUT | `/api/ordenes/{id}/estado` | Administrador, vendedor |

Las reglas de acceso están todas en `SeguridadConfig`.

## Lógica de negocio

- **Compra (`OrdenesService`)**: el frontend manda solo código y cantidad. El servidor revisa que haya stock de todo el carrito, lo descuenta y calcula el total con los precios de la base, así el precio no se puede manipular desde el navegador. Cada orden guarda el nombre y precio del momento de la compra.
- **Cancelar una orden** devuelve el stock al inventario.
- **Categorías**: no se puede eliminar una categoría que tiene productos.
- **Usuarios**: el correo y el RUN no se repiten; un administrador no puede eliminarse a sí mismo; el registro público solo acepta correos @duoc.cl, @profesor.duoc.cl y @gmail.com, y siempre crea clientes.

## Autenticación con JWT

1. `POST /api/auth/login` revisa la contraseña con BCrypt y devuelve:
   - un **access token** que dura 15 minutos, con el correo y el rol del usuario;
   - un **refresh token** que dura 7 días y solo sirve para renovar.
2. El frontend manda el access token en cada petición: `Authorization: Bearer <token>`.
3. `FiltroJwt` verifica la firma y la expiración. Si es válido, deja al usuario autenticado con su rol (`ROLE_ADMINISTRADOR`, `ROLE_VENDEDOR` o `ROLE_CLIENTE`) y `SeguridadConfig` decide si puede entrar. Sin token o con token vencido responde **401**; con un rol sin permiso, **403**.
4. Cuando el access token vence, el frontend llama a `POST /api/auth/refresh`. El refresh token tiene un id que se guarda en el usuario: al renovar se cambia por uno nuevo (rotación), y al cerrar sesión se borra. Por eso un refresh token ya usado o de una sesión cerrada no sirve.

Los tokens se firman con HMAC-SHA usando `JWT_SECRET`, que solo existe en el servidor. JJWT elige el algoritmo según el largo de la clave (con una clave de 64 bytes usa HS512).

## Errores

Los errores se responden en formato ProblemDetail, por ejemplo:

```json
{ "status": 409, "title": "Conflict", "detail": "Stock insuficiente de Araña Moderna Cristal Line (quedan 3)" }
```
