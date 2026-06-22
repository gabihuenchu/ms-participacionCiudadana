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
