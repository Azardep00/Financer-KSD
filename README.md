# Financer KSD API

Backend de asesoría financiera. El **asesor** arma el diagnóstico y un **plan mensual** con metas (de texto o medibles con monto); el **cliente** ve su progreso y marca metas cumplidas. Cada asesor lleva a sus clientes asignados, con notas privadas y un panel de seguimiento con alertas.

Stack: Java 21 · Spring Boot 3.3 · Spring Security + JWT · JPA/Hibernate · PostgreSQL · Swagger.

## Arrancar

```bash
docker compose up -d          # Postgres local
./mvnw spring-boot:run        # o: mvn spring-boot:run
mvn test                      # tests unitarios
```

- Swagger: http://localhost:8080/swagger-ui.html (botón **Authorize** → pega el token)
- Asesor inicial (se crea si la BD está vacía): `admin@financerksd.com` / `Admin123!` ← **cámbialo en producción**

Variables de entorno: `DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET` (mín. 32 caracteres), `CORS_ORIGINS`, `ADMIN_CORREO`, `ADMIN_CONTRASENA`, `PORT`, `APP_ZONA_HORARIA` (por defecto `America/Bogota`; define "hoy" para cierres de mes y alertas).

Actualizando desde una versión anterior: lee `docs/GUIA-MIGRACION.md`. Si tienes un frontend: `docs/NOTA-IMPACTO-FRONTEND.md`.

## Conceptos

- **Plan mensual**: cada plan pertenece a un mes (`periodo` = `yyyy-MM`) y tiene estado `ACTIVO`, `CERRADO` o `CANCELADO`. Un cliente solo puede tener **un plan activo** a la vez. Un plan cerrado o cancelado es de solo lectura.
- **Cierre de mes**: cierra el plan y, por defecto, crea el plan del mes siguiente arrastrando las metas pendientes (conservan su punto de partida).
- **Metas**: `TEXTO` (se marca a mano) o medibles (`AHORRO`, `REDUCCION_DEUDA`, `REDUCCION_GASTO`) con `montoObjetivo` y `valorInicial`. Las medibles se actualizan solas con cada diagnóstico nuevo y se completan al llegar al 100 %.
- **Estado del cliente**: `PROSPECTO`, `ACTIVO`, `PAUSADO`, `FINALIZADO`. Al crear su primer plan, un prospecto pasa a `ACTIVO`.
- **Permisos por asesor**: un asesor solo **escribe** sobre sus clientes asignados (o sobre los que aún no tienen asesor: queda asignado el primero que les crea un diagnóstico o un plan). Otro asesor recibe `403`. Para **leer**, cualquier asesor ve a cualquier cliente. El cliente solo ve y marca lo suyo.
- **Notas privadas**: solo asesores; el cliente recibe `403`. Solo quien escribió una nota puede borrarla.

## Endpoints

| Método | Ruta | Quién |
|---|---|---|
| POST | `/api/usuarios/clientes` | público (registro) |
| POST | `/api/usuarios/login` | público |
| POST | `/api/usuarios/asesores` | asesor |
| GET | `/api/usuarios/me` | logueado |
| GET | `/api/clientes?soloMios=false` | asesor |
| GET | `/api/clientes/panel` | asesor (ver abajo) |
| GET | `/api/clientes/{id}` | asesor / el propio cliente |
| PATCH | `/api/clientes/{id}/asesor` `{idAsesor}` | asesor responsable |
| PATCH | `/api/clientes/{id}/estado` `{estado}` | asesor responsable |
| POST | `/api/clientes/{id}/diagnosticos` | asesor responsable |
| GET | `/api/clientes/{id}/diagnosticos` · `/ultimo` | asesor / dueño |
| POST | `/api/clientes/{id}/planes` | asesor responsable |
| GET | `/api/clientes/{id}/planes` · `/planes/activo` | asesor / dueño |
| GET | `/api/clientes/{id}/progreso` | asesor / dueño |
| GET · POST | `/api/clientes/{id}/notas` | asesor responsable |
| DELETE | `/api/notas/{id}` | autor de la nota |
| GET | `/api/planes/{id}` | asesor / dueño |
| PUT | `/api/planes/{id}` | asesor responsable |
| POST | `/api/planes/{id}/cerrar` | asesor responsable |
| POST | `/api/planes/{id}/cancelar` | asesor responsable |
| POST | `/api/planes/{id}/metas` | asesor responsable |
| PUT · DELETE | `/api/metas/{id}` | asesor responsable |
| PATCH | `/api/metas/{id}/completar` · `/pendiente` | asesor responsable / dueño |

"Asesor responsable" = el asesor asignado al cliente, o cualquier asesor si el cliente aún no tiene uno. Errores: cuerpo `{ "mensaje": "..." }` con 400 (validación o regla de negocio), 401, 403 o 404.

## Ejemplos

```jsonc
// POST /api/clientes/2/diagnosticos
{ "ingresoMensual": 4000000, "gastoMensual": 3000000, "deudaTotal": 12000000, "ahorroActual": 6000000 }

// POST /api/clientes/2/planes   (periodo: cualquier día del mes; si falta, es el mes actual)
{ "titulo": "Plan noviembre", "descripcion": "Ordenar deudas y crear fondo", "periodo": "2026-11-01",
  "metas": [
    { "descripcion": "Llamar al banco", "fechaLimite": "2026-11-15" },
    { "descripcion": "Fondo de emergencia", "tipo": "AHORRO", "valorInicial": 6000000, "montoObjetivo": 9000000, "fechaLimite": "2026-11-30" } ] }

// POST /api/planes/7/cerrar   (todo opcional; por defecto crea el mes siguiente con las metas pendientes)
{ "notaCierre": "Buen mes", "crearSiguiente": true, "arrastrarPendientes": true, "tituloSiguiente": "Plan diciembre" }
// -> { "cierre": { idPlan, periodo, progreso, metasTotal, metasCumplidas, metasPendientes, metasArrastradas }, "siguientePlan": {...} }
```

## Panel de clientes (asesor)

`GET /api/clientes/panel?q=&estado=&mios=&sinAsesor=&conAlertas=&orden=alertas|nombre|diagnostico&pagina=0&tamano=20`

- `q`: busca por nombre, apellido, correo o teléfono, sin importar tildes ni mayúsculas ("juan perez" encuentra a Juan Pérez).
- `mios=true` solo tus clientes; `sinAsesor=true` solo los que no tienen asesor (no se pueden usar juntos). `tamano` entre 1 y 100.
- `resumen`: totales por estado, con alertas, sin asesor y alertas por tipo, calculados sobre el alcance elegido **antes** de buscar o filtrar por estado.
- Cada cliente trae estado, asesor, plan activo (progreso, metas pendientes y vencidas), último diagnóstico, cantidad de notas y alertas.
- Alertas (solo clientes `ACTIVO`): `SIN_DIAGNOSTICO`, `DIAGNOSTICO_VIEJO` (30 días o más), `SIN_PLAN_ACTIVO`, `MES_SIN_CERRAR` (el mes del plan ya terminó), `METAS_VENCIDAS`.
- Búsqueda, orden y paginación se hacen en memoria: bien para cientos o pocos miles de clientes. Si crecen mucho, hay que moverlos a la base de datos.

## Métricas (calculadas en `DiagnosticoFinanciero`)

- **Tasa de ahorro** = (ingreso − gasto) / ingreso × 100
- **Deuda/ingreso anual** = deuda / (ingreso × 12) × 100 (alerta sobre ~36%)
- **Meses de fondo de emergencia** = ahorro / gasto mensual

## Estructura

```
com.financerksd.api
├── config/       DataInitializer (asesor inicial), MigracionDatosInicial, RelojConfig, OpenApiConfig
├── controller/   Usuario, Cliente, Panel, Plan, Meta, Nota
├── dto/          requests/responses (records)
├── exception/    excepciones + GlobalExceptionHandler
├── model/        Usuario (Asesor | Cliente), DiagnosticoFinanciero, PlanMejora, MetaFinanciera, NotaAsesor
├── repository/   Spring Data JPA
├── security/     JWT, filtro, SecurityConfig, respuestas 401/403
└── service/      lógica de negocio, AutorizacionService, ReglasAcceso, AlertasCliente, PanelFiltros, PanelClientesService
```

Los CORS permiten `localhost:4200` (Angular) y `localhost:5173` (React). Al desplegar, define `CORS_ORIGINS` con las URLs reales.
