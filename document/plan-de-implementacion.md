# Plan de implementación — ms-citizen (Fase 3)

**Microservicio:** Participación Ciudadana  
**Puerto:** 8084  
**Estado:** 🔄 En progreso

## Backend — Checklist

- [x] Proyecto Spring Boot 3.4 + Java 21
- [x] Entidades JPA: `Necesidad`, `Donacion`, `ItemDonacion`
- [x] Migraciones Flyway: `V1__create_needs_table.sql`, `V2__create_donations_table.sql`, `V3__create_donation_items_table.sql`
- [x] APIs REST en español: `/necesidades/**`, `/donaciones/**`
- [x] Seguridad Firebase + `DevAuthFilter` para local
- [x] Publicación eventos: `need.created`, `donation.created`, `donation.confirmed`
- [x] Consumidor `stock.critical` → necesidad automática
- [x] RFC 7807 en `GlobalExceptionHandler`
- [x] Convención de nombres CLAUDE.md: `NecesidadController`, `DonacionService`, etc.
- [ ] Integración E2E vía `ms-gateway` en entorno local completo
- [ ] Cobertura tests ≥ 80% en capa Service

## Integración

- [x] Ruta gateway: `/necesidades/**`, `/donaciones/**` → `ms-citizen:8084`
- [x] Servicios/hooks en `frontend-info` (`citizen.service.ts`, `usePublicNeeds`, `useDonations`)
- [ ] UI wizard de donación en frontend-info

### MS-Notificacion (Kamilo14) — eventos RabbitMQ

Rama Gitflow: `feature/integracion-participacion-ciudadana` → PR a `develop`.

| Rol | Routing key | Exchange |
|-----|-------------|----------|
| **Publica** ms-citizen | `donation.created`, `donation.confirmed`, `need.created` | `catastrofescl.events` |
| **Consume** ms-citizen | `stock.critical` | cola `stock.critical.queue` |
| **Consume** [MS-Notificacion](https://github.com/Kamilo14/MS-Notificacion) | todos (`notifications.queue` binding `#`) | mismo exchange |

Campos clave para notificaciones in-app (`ProcesadorEventosNotificacion`):

- `donation.created` / `donation.confirmed` → `eventId`, `usuarioDonanteId`, `codigoQr`, `donadoEn` / `confirmadoEn`
- `need.created` (manual) → `eventId`, `usuarioId`

Validación local: levantar RabbitMQ + ms-citizen + MS-Notificacion en `:8086`; registrar donación y ver mensaje en cola `notifications.queue`.
