
# 🏋️‍♂️ GymFex — Plataforma modular de gestión de socios

GymFex es una plataforma modular basada en microservicios para la gestión de socios y administradores en gimnasios. Utiliza Kafka como bus de eventos, Postgres como base de datos, y SMTP para notificaciones por correo electrónico.

---

## 📦 Estructura del repositorio

```
/
├─ usuarios-service/           # Servicio REST para gestión de usuarios y autenticación JWT
│  ├─ src/
│  ├─ Dockerfile
│  └─ pom.xml
├─ notificaciones-service/     # Servicio Kafka consumer para envío de correos
│  ├─ src/
│  ├─ Dockerfile
│  └─ pom.xml
├─ gymfex-common-events/       # DTOs compartidos para eventos Kafka
├─ docker-compose.yml          # Orquestación de servicios
├─ docker/                     # Scripts y configuración adicional
└─ README.md                   
```

---

## 🚀 Requisitos

- Java 11+
- Maven (o Gradle)
- Docker & Docker Compose
- Servidor SMTP accesible (configurable por variables de entorno)

---

## 🧩 Arquitectura

| Servicio               | Descripción                                                                 |
|------------------------|------------------------------------------------------------------------------|
| usuarios-service       | API REST con JWT. Endpoints para autenticación y CRUD de usuarios.          |
| notificaciones-service | Kafka consumer que procesa eventos de usuarios y envía correos.             |
| Kafka + Zookeeper      | Bus de eventos distribuido.                                                 |
| Postgres               | Persistencia de usuarios.                                                   |
| SonarQube              | Análisis estático de código.                                                |

---

## ⚙️ Docker Compose

Servicios incluidos:

- Zookeeper: `2181`
- Kafka: `9092`
- Postgres: `5432` (inicializado con `docker/init.sql`)
- usuarios-service: `8081` (mapea `8081:8080`)
- notificaciones-service: `8082` (mapea `8082:8080`)
- SonarQube: `9000`

---

## 🌱 Variables de entorno necesarias
Configura las siguientes variables de entorno antes de ejecutar los servicios.

🔐 Seguridad y JWT

JWT_SECRET — Clave secreta para firmar tokens JWT.
JWT_EXPIRATION_MS — Tiempo de expiración del token en milisegundos.

🗄️ Base de datos (Postgres)

DB_URL — URL JDBC de conexión (ej. jdbc:postgresql://postgres-db:5432/usuariosdb)
DB_USERNAME — Usuario de la base de datos.
DB_PASSWORD — Contraseña de la base de datos.

📬 SMTP (notificaciones-service)

SMTP_HOST — Host del servidor SMTP
SMTP_PORT — Puerto SMTP (ej. 587)
SMTP_USERNAME — Usuario SMTP
SMTP_PASSWORD — Contraseña SMTP
SMTP_FROM — Dirección del remitente 

## 🧪 Ejecución local

### Con Docker

```bash
git clone https://github.com/Abdel201422/GymFex.git
cd GymFex
docker-compose up --build
```

## 🔐 API usuarios-service

### AuthController — `/auth`

- `POST /auth/register/admin` — Registra admin y devuelve JWT.
- `POST /auth/login` — Login y devuelve JWT.

### UsuarioController — `/usuarios`

- `GET /usuarios/administradores` — Lista administradores.
- `GET /usuarios/socios` — Lista socios.
- `GET /usuarios/{id}` — Obtener usuario por ID.
- `GET /usuarios/search?nombre=...` — Buscar por nombre.
- `POST /usuarios/socio` — Crear socio.
- `POST /usuarios/admin` — Crear admin.
- `PUT /usuarios/socio/{id}` — Actualizar socio.
- `PUT /usuarios/admin/{id}` — Actualizar admin.
- `DELETE /usuarios/{id}` — Eliminar usuario.

---

## 📡 Eventos Kafka

- Tipos: `SOCIO_CREATED`, `SOCIO_UPDATED`, `SOCIO_DELETED`
- Productor: `usuarios-service`
- Consumidor: `notificaciones-service`

---

## 📬 Notificaciones

- SMTP configurable por entorno.
- Idempotencia: evita reprocesar eventos duplicados.
- Manejo de errores: marca evento como procesado si falla envío.
