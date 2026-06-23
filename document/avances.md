# Avances — ms-citizen

| Área | Estado |
|------|--------|
| Backend APIs | ✅ |
| Flyway / BD | ✅ |
| RabbitMQ productor/consumidor | ✅ |
| Seguridad Firebase + dev | ✅ |
| Tests unitarios | ✅ (18 tests Mockito/JUnit) |
| Tests integración | 🔄 (requiere Docker: `mvn test -Pintegration`) |
| Gateway routing | ✅ (repo ms-gateway) |
| Frontend hooks | ✅ (repo frontend-info) |
| Docker local | ✅ |

## [2026-06-06] Alineación CLAUDE.md y correcciones de estándares

**Integrante(s):** Gabriela Huenchullán  
**Fase trabajada:** Fase 3 — Participación Ciudadana

### Completado

- Renombrado Controller/Service/Repository/Mapper según convención del proyecto
- RFC 7807 corregido (`type`, `timestamp`, `errorCode`)
- Estructura `document/` con archivos de control
- Tests reorganizados en `unit/` e `integration/`
- Dockerfile multi-stage para entorno local sin Maven

### Próximos pasos

- Integración E2E local con `ms-gateway` + `frontend-info`
- Wizard de donación en frontend
- Ampliar cobertura de tests
