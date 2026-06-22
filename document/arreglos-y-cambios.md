# Arreglos y cambios — ms-citizen

### [ARR-001] Dockerfile multi-stage para build sin Maven local

- **Fecha:** 2026-06-05
- **Autor:** Gabriela Huenchullán
- **Tipo:** Config
- **Descripción:** Se reemplazó el Dockerfile de copia de JAR por build multi-stage (`maven:3.9-eclipse-temurin-21-alpine` → `eclipse-temurin:21-jre-alpine`). Permite `docker compose up --build` sin Java 21 ni Maven en el host.
- **Archivos afectados:** `Dockerfile`, `.dockerignore`
- **Tests actualizados:** N/A

### [ARR-002] Java 21 vía Eclipse Temurin en lugar de Microsoft OpenJDK winget

- **Fecha:** 2026-06-05
- **Autor:** Gabriela Huenchullán
- **Tipo:** Docs | Entorno
- **Descripción:** `winget install Microsoft.OpenJDK.21` falla por hash inválido. Alternativa validada: `winget install EclipseAdoptium.Temurin.21.JDK`.
- **Archivos afectados:** N/A (entorno local)
- **Tests actualizados:** N/A

### [ARR-003] Alineación de nombres de capa con CLAUDE.md

- **Fecha:** 2026-06-06
- **Autor:** Gabriela Huenchullán
- **Tipo:** Refactor
- **Descripción:** Renombradas clases de capa arquitectónica al español de dominio según cheatsheet CLAUDE.md: `NecesidadController`, `DonacionController`, `NecesidadService`, `DonacionService`, `NecesidadRepository`, `DonacionRepository`, mappers correspondientes. Tests movidos a `unit/` e `integration/`.
- **Archivos afectados:** `controller/`, `service/`, `repository/`, `event/StockCriticalConsumer.java`, `exception/GlobalExceptionHandler.java`, `src/test/`
- **Tests actualizados:** Sí

### [ARR-004] RFC 7807 completo en GlobalExceptionHandler

- **Fecha:** 2026-06-06
- **Autor:** Gabriela Huenchullán
- **Tipo:** Fix
- **Descripción:** Errores ahora incluyen `type` (`https://catastrofescl.cl/errors/{slug}`), `errorCode`, `timestamp` e `instance`. Agregado handler para `NeedNotFoundException`.
- **Archivos afectados:** `exception/GlobalExceptionHandler.java`
- **Tests actualizados:** N/A

### [ARR-005] Documentación de control del proyecto en document/

- **Fecha:** 2026-06-06
- **Autor:** Gabriela Huenchullán
- **Tipo:** Docs
- **Descripción:** `CLAUDE.md` movido de `src/document/` a `document/` en la raíz del repo. Creados `plan-de-implementacion.md`, `errores.md`, `avances.md` específicos de ms-citizen.
- **Archivos afectados:** `document/*`
- **Tests actualizados:** N/A
