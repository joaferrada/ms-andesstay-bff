# ms-andesstay-bff

Backend for Frontend de AndesStay (puerto **8080**). Spring Boot 4.1.1 · Java 17 · Resource Server JWT (Microsoft Entra ID). Recibe las llamadas del frontend a traves de **AWS API Gateway**, autoriza por rol y reenvia el JWT a `ms-andesstay-reservations` (`:8081`).

```
Frontend (MSAL) --> API Gateway (CORS + JWT authorizer) --> BFF :8080 (roles) --> reservations :8081 (roles + dueño)
```

## Ejecutar

```powershell
# Requiere ms-andesstay-reservations corriendo en :8081
.\mvnw.cmd spring-boot:run
```

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

"Solo las suyas" lo aplica `ms-andesstay-reservations` (una reserva ajena responde 404). Los 4xx del microservicio (400, 403, 404, 409) se devuelven tal cual con su JSON (`exception/MicroserviceErrorHandler`).

## Tests

```powershell
.\mvnw.cmd clean package
```

- `ReservationsBffControllerSecurityTest`: cada endpoint con `jwt()` y `ROLE_*` de los 4 roles + sin rol (200/204 o 403), sin token (401) y propagacion de un 403 del microservicio.
