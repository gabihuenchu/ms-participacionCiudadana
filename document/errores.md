# Errores — ms-citizen

## Abiertos

_(ninguno)_

## Resueltos

### [ERR-001] Maven Wrapper falla — powershell no en PATH

- **Fecha:** 2026-06-05
- **Severidad:** 🟡 Media
- **Descripción:** `mvnw.cmd` no encuentra `powershell.exe` porque `C:\Windows\System32` falta en PATH del usuario.
- **Causa raíz:** PATH de Windows incompleto en terminal del desarrollador.
- **Estado:** Resuelto — workaround: Dockerfile multi-stage compila dentro de Docker sin Maven local.
- **Referencia:** [ARR-001](arreglos-y-cambios.md#arr-001)

### [ERR-002] Docker build falla — JAR inexistente en target/

- **Fecha:** 2026-06-05
- **Severidad:** 🟡 Media
- **Descripción:** `COPY target/ms-citizen-*.jar` fallaba porque no se había ejecutado `mvn package`.
- **Causa raíz:** Dockerfile original asumía build previo en host.
- **Estado:** Resuelto — Dockerfile multi-stage.
- **Referencia:** [ARR-001](arreglos-y-cambios.md#arr-001)

### [ERR-003] winget Microsoft.OpenJDK.21 — hash no coincide

- **Fecha:** 2026-06-05
- **Severidad:** 🟢 Baja
- **Descripción:** Instalador de Microsoft OpenJDK 21 descarga solo 181 KB; hash SHA256 no coincide.
- **Causa raíz:** Manifiesto winget desactualizado o URL rota en `Microsoft.OpenJDK.21`.
- **Estado:** Resuelto — usar `EclipseAdoptium.Temurin.21.JDK` como alternativa.
- **Referencia:** [ARR-002](arreglos-y-cambios.md#arr-002)
