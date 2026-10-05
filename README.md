# Financer KSD API

Backend de asesoría financiera: el **asesor** arma el diagnóstico y el plan de mejora; el **cliente** ve su progreso y marca metas cumplidas.

Stack: Java 21 · Spring Boot 3.3 · Spring Security + JWT · JPA/Hibernate · PostgreSQL · Swagger.

## Arrancar

```bash
docker compose up -d          # Postgres local
./mvnw spring-boot:run        # o: mvn spring-boot:run
```

- Swagger: http://localhost:8080/swagger-ui.html (botón **Authorize** → pega el token)
- Asesor inicial (se crea si la BD está vacía): `admin@financerksd.com` / `Admin123!`  ← **cámbialo en producción**

Variables de entorno: `DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET` (mín. 32 caracteres), `CORS_ORIGINS`, `ADMIN_CORREO`, `ADMIN_CONTRASENA`, `PORT`.

## Estructura

```
com.financerksd.api
├── config/       DataInitializer (asesor inicial), OpenApiConfig
├── controller/   Usuario, Cliente, Plan, Meta
├── dto/          requests/responses (records)
├── exception/    excepciones + GlobalExceptionHandler
├── model/        Usuario (Asesor | Cliente), DiagnosticoFinanciero, PlanMejora, MetaFinanciera
├── repository/   Spring Data JPA
├── security/     JWT, filtro, SecurityConfig, respuestas 401/403
└── service/      lógica de negocio + AutorizacionService (dueño del recurso)
```

## Endpoints

| Método | Ruta | Quién |
|---|---|---|
| POST | `/api/usuarios/clientes` | público (registro) |
| POST | `/api/usuarios/login` | público |
| POST | `/api/usuarios/asesores` | asesor |
| GET | `/api/usuarios/me` | logueado |
| GET | `/api/clientes?soloMios=false` | asesor |
| GET | `/api/clientes/{id}` | asesor / el propio cliente |
| PATCH | `/api/clientes/{id}/asesor` `{idAsesor}` | asesor |
| POST | `/api/clientes/{id}/diagnosticos` | asesor |
| GET | `/api/clientes/{id}/diagnosticos` · `/ultimo` | asesor / dueño |
| POST | `/api/clientes/{id}/planes` | asesor |
| GET | `/api/clientes/{id}/planes` · `/planes/activo` | asesor / dueño |
| GET | `/api/clientes/{id}/progreso` | asesor / dueño |
| GET | `/api/planes/{id}` | asesor / dueño |
| PUT | `/api/planes/{id}` | asesor |
| POST | `/api/planes/{id}/metas` | asesor |
| PUT · DELETE | `/api/metas/{id}` | asesor |
| PATCH | `/api/metas/{id}/completar` · `/pendiente` | asesor / dueño |

## Ejemplos

```jsonc
// POST /api/clientes/2/diagnosticos
{ "ingresoMensual": 4000000, "gastoMensual": 3000000, "deudaTotal": 12000000, "ahorroActual": 6000000 }

// POST /api/clientes/2/planes
{ "titulo": "Plan 6 meses", "descripcion": "Ordenar deudas y crear fondo",
  "metas": [ { "descripcion": "Fondo de emergencia de 3 meses", "fechaLimite": "2027-03-31" } ] }
```

## Métricas (calculadas en `DiagnosticoFinanciero`)

- **Tasa de ahorro** = (ingreso − gasto) / ingreso × 100
- **Deuda/ingreso anual** = deuda / (ingreso × 12) × 100 (alerta sobre ~36%)
- **Meses de fondo de emergencia** = ahorro / gasto mensual

## Siguiente paso: frontends

Los CORS ya permiten `localhost:4200` (Angular) y `localhost:5173` (React). Al desplegar, define `CORS_ORIGINS` con las URLs reales.
