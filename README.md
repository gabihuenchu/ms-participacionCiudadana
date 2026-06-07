# ms-citizen — Participación Ciudadana

Microservicio **MS-4** del ecosistema CatástrofesCL. Gestiona necesidades de recursos y donaciones ciudadanas.

| Atributo | Valor |
|----------|-------|
| Puerto | `8084` |
| Gateway | `/necesidades/**`, `/donaciones/**` → `http://ms-citizen:8084` |
| Base de datos | `catastrofescl_citizen` (PostgreSQL) |

## APIs críticas

| Método | Ruta | Auth | Permiso |
|--------|------|------|---------|
| `GET` | `/necesidades/publicas` | No | — |
| `GET` | `/necesidades/centro/{centroId}` | No | — |
| `POST` | `/necesidades` | Sí | `NECESIDAD_GESTIONAR` |
| `POST` | `/donaciones` | Sí | `DONACION_REALIZAR` |
| `POST` | `/donaciones/{codigoQr}/confirmar` | Sí | `DONACION_CONFIRMAR` |
| `GET` | `/donaciones/mis-contribuciones` | Sí | Usuario autenticado |

> Las rutas usan **español** según la convención del proyecto (`/necesidades/publicas` equivale a `/needs/public`).

## Eventos RabbitMQ

**Publica:** `need.created`, `donation.created`, `donation.confirmed`  
**Consume:** `stock.critical` → crea necesidad automática

## Desarrollo local

### Requisitos

- Java 21
- Maven 3.9+
- PostgreSQL 15
- RabbitMQ (opcional para consumidor de eventos)

### Variables de entorno

```bash
DATABASE_URL=jdbc:postgresql://localhost:5432/catastrofescl_citizen
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=postgres
RABBITMQ_HOST=localhost
FIREBASE_ENABLED=false
```

### Ejecutar

```bash
mvn spring-boot:run
```

Swagger UI: http://localhost:8084/swagger-ui.html

### Modo desarrollo (sin Firebase)

Usar headers de prueba como en `ms-emergencies`:

```bash
curl -X POST http://localhost:8084/donaciones \
  -H "Content-Type: application/json" \
  -H "X-Dev-User-Id: 550e8400-e29b-41d4-a716-446655440000" \
  -H "X-Dev-Permissions: DONACION_REALIZAR" \
  -d '{
    "centroId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "items": [{ "itemId": "b2c3d4e5-f6a7-8901-bcde-f12345678901", "cantidad": 3 }]
  }'
```

## Integración con frontend-info

El portal ciudadano (`frontend-info`) consumirá estos endpoints vía API Gateway (`NEXT_PUBLIC_API_URL`, puerto `8080`):

```typescript
// Servicios planificados en frontend-info
GET  /necesidades/publicas          → usePublicNeeds
GET  /necesidades/centro/:id        → wizard paso 2
POST /donaciones                    → DonationForm
GET  /donaciones/mis-contribuciones → panel de impacto
```

## Estructura

```
src/main/java/cl/catastrofescl/citizen/
├── controller/     NecesidadController, DonacionController
├── service/        NecesidadService, DonacionService
├── entity/         Necesidad, Donacion, ItemDonacion
├── event/          RabbitMQ consumers y payloads
├── config/         Security, RabbitMQ, Firebase
└── exception/      RFC 7807 ProblemDetail

document/           CLAUDE.md, plan, errores, avances, arreglos
```

## Tests

```bash
# Requiere Java 21 en PATH y System32 accesible (para mvnw)
.\mvnw.cmd test
```

## Docker

El Dockerfile compila el proyecto dentro de Docker (multi-stage). No necesitas `mvn package` previo:

```bash
docker compose up --build
```

PostgreSQL expone el puerto **5433** en el host (internamente `5432`).
