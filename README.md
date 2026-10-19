# ms-andesstay-bff

Backend for Frontend de AndesStay (puerto **8080**). Spring Boot 4.1.1 · Java 17 · Resource Server JWT (Microsoft Entra ID). Recibe las llamadas del frontend a traves de **AWS API Gateway**, autoriza por rol y reenvia el JWT al microservicio que corresponde.

```
Frontend (MSAL) --> API Gateway (CORS + JWT authorizer) --> BFF :8080 (roles) --> /api/reservations/** -> reservations :8081 (roles + dueño)
                                                                              --> /api/catalog/**      -> catalog :8085
                                                                              --> /api/audit/**        -> audit :8086 (solo GET)
                                                                              --> /api/report/**       -> report :8087
```

## Ejecutar

```powershell
# Requiere los microservicios corriendo (por defecto en localhost)
.\mvnw.cmd spring-boot:run
```

Las URLs internas estan en `application.yaml` (`andesstay.services.*.url`) y se cambian con variables de entorno; en Docker se usan los nombres de servicio de infra/compose.yml:

| Variable | Default | Ejemplo en Docker |
|---|---|---|
| `RESERVATIONS_URL` | `http://localhost:8081` | `http://reservations:8081` |
| `CATALOG_URL` | `http://localhost:8085` | `http://catalog:8085` |
| `AUDIT_URL` | `http://localhost:8086` | `http://audit:8086` |
| `REPORT_URL` | `http://localhost:8087` | `http://report:8087` |

## CORS y seguridad en API Gateway

El BFF **no configura CORS**: lo resuelve el **HTTP API** de AWS API Gateway, que responde los preflight `OPTIONS` antes de llegar al BFF. Por eso el frontend en `localhost:4200` debe apuntar a la URL del API Gateway, no a `localhost:8080`.

**CORS** (API Gateway > HTTP API > CORS):

| Campo | Valor |
|---|---|
| Access-Control-Allow-Origin | `http://localhost:4200`, `https://joaferrada.github.io` |
| Access-Control-Allow-Methods | `GET, POST, PUT, DELETE, OPTIONS` |
| Access-Control-Allow-Headers | `authorization, content-type` |
| Access-Control-Allow-Credentials | `true` |

**JWT authorizer** (API Gateway > HTTP API > Authorization), asociado a las rutas `ANY /api/{proxy+}`:

| Campo | Valor |
|---|---|
| Nombre | `entra-jwt` |
| Identity source | `$request.header.Authorization` |
| Issuer | `https://sts.windows.net/<tenant>/` |
| Audience | `api://<clientId>` |

- El token lo emite Entra ID para el scope `api://<clientId>/access_as_user` (token v1, por eso el issuer es `sts.windows.net`).
- API Gateway rechaza con **401** un token ausente, vencido o de otro issuer/audience. El authorizer no revisa roles: eso lo hace el BFF.
- El BFF vuelve a validar el JWT (`issuer-uri` y `jwk-set-uri` en `application.yaml`) y mapea el claim `roles` a `ROLE_*` en `security/JwtAuthConverter`.

## Roles y permisos

App roles definidos en el App registration de Entra ID (claim `roles`). Se aplican con `@PreAuthorize` en `ReservationsBffController`; si no se cumplen, **403**.

| Endpoint | Admin | Operador_Dominio | Cliente_Dominio | Auditor |
|---|---|---|---|---|
| `GET /api/reservations` (y `?status=`) | si | si | si (solo las suyas) | si |
| `GET /api/reservations/{id}` | si | si | si (solo las suyas) | si |
| `POST /api/reservations` | si | si | si | no |
| `PUT /api/reservations/{id}` | si | si | si (solo las suyas) | no |
| `PUT /api/reservations/{id}/status` | si | si | no | no |
| `DELETE /api/reservations/{id}` | si | no | no | no |

| `GET /api/catalog/units` (y `?tipo=&hostal=&soloDisponibles=`), `GET /api/catalog/units/{id}` | si | si | si | si |
| `POST /api/catalog/units`, `PUT` / `DELETE /api/catalog/units/{id}` | si | no | no | no |
| `POST /api/catalog/units/{id}/reserve` y `/release` | si | si | no | no |
| `GET /api/audit/events` (filtros y paginacion), `GET /api/audit/reservations/{id}/timeline` | si | no | no | si |
| `GET /api/report/kpis?range=`, `GET /api/report/top-units?range=&limit=` | si | no | no | no |

`POST /api/audit/events` **no se expone** en el BFF (405): es interno, lo llama reservations al crear, cambiar de estado o eliminar.

"Solo las suyas" lo aplica `ms-andesstay-reservations` (una reserva ajena responde 404, comparando `huespedEmail` con el principal del token).

El BFF reenvia body y respuesta como `Map<String, Object>` sin transformarlos, asi que los campos del huesped (`huespedNombre`, `huespedEmail`, `creadoPor`) pasan tal cual. Quien decide de donde salen los datos del huesped (token para Cliente, body para Admin/Operador) es el microservicio. Los 4xx y 5xx del microservicio (400, 403, 404, 409, 503) se devuelven tal cual con su JSON, y si el microservicio no responde el BFF devuelve **503** (`exception/MicroserviceErrorHandler`).

## Docker

`Dockerfile` multi-stage: build con `maven:3.9-eclipse-temurin-17` y runtime `eclipse-temurin:17-jre` con usuario no root, puerto **8080** y `JAVA_TOOL_OPTIONS=-XX:MaxRAMPercentage=75`. Se levanta junto al resto del backend con el compose unico de `infra` (ver su README):

```powershell
cd ..\infra
docker compose up -d --build bff
docker compose logs -f bff
```

En el compose apunta a `http://reservations:8081`, `http://catalog:8085`, `http://audit:8086` y `http://report:8087`; arranca cuando reservations esta healthy. Healthcheck: `GET http://localhost:8080/actuator/health`.

## Tests

```powershell
.\mvnw.cmd clean package
```

- `ReservationsBffControllerSecurityTest`: cada endpoint con `jwt()` y `ROLE_*` de los 4 roles + sin rol (200/204 o 403), sin token (401) y propagacion de un 403 del microservicio.
- `CatalogAuditReportBffSecurityTest`: lo mismo para catalog, audit y report; POST de audit no expuesto (405), 409 de catalog y 503 de report propagados, microservicio caido -> 503.
- `ServiceUrlsTest`: cada service usa `andesstay.services.*.url` y reenvia solo los query params informados.
