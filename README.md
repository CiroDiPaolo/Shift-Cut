# Shift Cut - Sistema de Turnos para Barbería

API REST para gestión de turnos de una barbería, construida con **Spring Boot**, **Spring Security + JWT**, **Spring Data JPA** e **Hibernate**, contenerizada con **Docker**.

---

## Tecnologías

| Tecnología | Versión |
|---|---|
| Java | 17 |
| Spring Boot | 4.0.3 |
| Spring Security | 7.x |
| JWT (jjwt) | 0.11.5 |
| MySQL | 8.4 |
| Lombok | latest |
| Docker / Docker Compose | latest |
| Springdoc OpenAPI (Swagger) | 2.8.6 |

---

## Recomendaciones sobre JWT

- En producción **asegurate** de proveer una clave JWT (`jwt.secret`) con al menos 32 bytes (256 bits). El proyecto contiene una medida temporal para tests que deriva SHA-256 del secreto cuando es muy corto, pero en producción debe usarse una clave segura y larga.

---

## Usuario administrador por defecto

La primera vez que se levanta el sistema, se crea automáticamente un usuario administrador con las siguientes credenciales:

| Campo | Valor |
|---|---|
| Username | `Admin` |
| Email | `admin@admin.com` |
| Contraseña | `Admin123` |
| Rol | `ADMIN` |

> ⚠️ **Importante:** Cambiá la contraseña del administrador en cuanto el sistema esté en producción.

Con este usuario podés:
- Crear usuarios con rol `BARBER` (actualizando el rol desde `PUT /api/user/{id}`)
- Gestionar todos los turnos y usuarios del sistema
- Acceder a todos los endpoints protegidos con rol `ADMIN`

---

## Requisitos previos

- Docker instalado y corriendo
- (Opcional para desarrollo local) JDK 17 + Maven 3.9+

---

## Configuración

### 1. Clonar el repositorio

```bash
git clone https://github.com/CiroDiPaolo/shift-cut.git
cd shift-cut
```

### 2. Crear el archivo `.env`

Copiá el archivo de ejemplo y completá los valores:

```bash
cp .env.example .env
```

Editá `.env` con tus propios valores:

```dotenv
DB_NAME=cuts
DB_USERNAME=root
DB_PASSWORD=tu_password_seguro

JWT_SECRET=tu_secreto_jwt_muy_largo_y_seguro

SERVER_PORT=8080
```

> **Importante:** El archivo `.env.example` que está en el repositorio es SOLO UN EJEMPLO y NO contiene secretos reales; el archivo `.env` con valores reales **no debe** subirse al repositorio (está incluido en `.gitignore`). Asegurate de no commitear `.env` ni tus secretos.

---

## Levantar con Docker

```bash
# Primera vez o cuando haya cambios en el código
docker compose up --build

# Veces siguientes
docker compose up

# Detener los contenedores
docker compose down
```

La API quedará disponible en: `http://localhost:8080`

---

## Documentación de la API (Swagger/OpenAPI)

Este proyecto incluye documentación interactiva de la API generada automáticamente con Swagger/OpenAPI.

- Accede a la documentación en: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- Puedes probar los endpoints directamente desde la interfaz web.
- La documentación se actualiza automáticamente con cada cambio en los controladores y modelos.

### ¿Qué cubre Swagger?
- Todos los endpoints REST principales (usuarios, turnos, autenticación).
- Modelos de datos y ejemplos de request/response.
- Códigos de respuesta y descripciones.

> **Nota:** Los tests unitarios y de integración no aparecen en Swagger, pero garantizan que la API funciona correctamente y cumple con los requisitos de negocio.

---

## Endpoints

### Autenticación (público)

| Método | Endpoint | Descripción |
|---|---|---|
| POST | `/auth/register` | Registro de nuevo usuario |
| POST | `/auth/login` | Login, devuelve JWT |

#### Ejemplo de registro
```json
{
  "username": "juanperez",
  "email": "juan@example.com",
  "password": "123456"
}
```

#### Ejemplo de login
```json
{
  "email": "juan@example.com",
  "password": "123456"
}
```

**Respuesta (ejemplo):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

---

### Usuarios (requiere Bearer Token)

| Método | Endpoint | Rol requerido | Descripción |
|---|---|---|---|
| GET | `/api/user/me` | USER / ADMIN | Obtener usuario autenticado |
| GET | `/api/user/{id}` | ADMIN | Obtener usuario por ID |
| GET | `/api/user` | ADMIN | Listar todos los usuarios |
| GET | `/api/user/email/{email}` | ADMIN | Buscar usuario por email |
| PUT | `/api/user/{id}` | ADMIN o propio USER | Actualizar usuario |
| DELETE | `/api/user/{id}` | ADMIN | Eliminar usuario |

### Ejemplo de actualización de usuario (PUT /api/user/{id})

#### Como ADMIN (puede cambiar cualquier campo, incluso rol y status):
```json
{
  "username": "barbero1",
  "email": "barbero1@barberia.com",
  "password": "nuevoPassword123",
  "role": "BARBER",
  "status": true
}
```

#### Como USER (solo puede cambiar username, email y password):
```json
{
  "username": "juanperez",
  "email": "juan_nuevo@example.com",
  "password": "nuevoPassword456"
}
```

Nota: Si se envía `role` o `status` en la petición como USER, serán ignorados por el controlador.

#### Respuesta exitosa:
```json
{
  "id": 3,
  "username": "juanperez",
  "email": "juan_nuevo@example.com",
  "role": "USER",
  "status": true
}
```

#### Errores posibles:
- 403 Forbidden: Si un usuario intenta modificar a otro usuario o cambiar su rol/status sin ser ADMIN.
- 409 Conflict: Si el email o username ya existen en otro usuario.
- 404 Not Found: Si el usuario no existe.

> **Nota:** La contraseña siempre se almacena encriptada. El campo "password" es opcional en la actualización: si no se envía, no se modifica.

---

### Turnos (requiere Bearer Token)

| Método | Endpoint | Rol requerido | Descripción |
|---|---|---|---|
| GET | `/api/appointment` | ADMIN | Listar todos los turnos |
| GET | `/api/appointment/{id}` | ADMIN | Obtener turno por ID |
| GET | `/api/appointment/user/{userId}` | ADMIN o propio USER | Turnos de un usuario |
| POST | `/api/appointment` | USER / ADMIN | Crear turno |
| PUT | `/api/appointment/{id}` | ADMIN | Actualizar turno |
| DELETE | `/api/appointment/{id}` | ADMIN | Eliminar turno |

---

## Roles

| Rol | Descripción |
|---|---|
| `USER` | Cliente de la barbería |
| `ADMIN` | Administrador del sistema |
| `BARBER` | Barbero (extensible) |

---

## Servicios disponibles

| Enum | Descripción |
|---|---|
| `HAIR_CUT` | Corte de cabello |
| `HAIR_CUT_AND_BEARD` | Corte de cabello y barba |

---

## Desarrollo local (sin Docker)

1. Asegurate de tener MySQL corriendo en `localhost:3306` con una base de datos llamada `cuts`
2. Configurá las variables de entorno o usá los valores de fallback de `application.properties`
3. Ejecutá:

```bash
./mvnw spring-boot:run
```

---

## Correr tests

Para ejecutar la suite de tests localmente (se puede setear `JWT_SECRET` temporalmente para pruebas):

```powershell
$env:JWT_SECRET='tu_secreto_largo_32_plus_bytes'; .\mvnw test
```

En CI/producción, exportá `JWT_SECRET` como variable de entorno segura antes de ejecutar la aplicación.

---

## Estructura del proyecto

```
src/main/java/com/shift_cut/
├── Config/
│   ├── Auth/          # Login, Register, AuthService, AuthController
│   └── Security/      # JWT Filter, JWT Service, Security Config
├── Control/           # Controllers REST
├── Exceptions/        # Excepciones personalizadas + GlobalExceptionHandler
├── Model/
│   ├── DTO/           # Data Transfer Objects
│   └── Enum/          # Role, ServiceType
├── Repository/        # Interfaces JPA
└── Service/           # Lógica de negocio
```
