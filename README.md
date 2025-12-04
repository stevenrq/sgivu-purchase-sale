# 🤝 SGIVU - Servicio de Compra/Venta

## 📘 Descripción

Microservicio encargado de centralizar los contratos de compra y venta de vehículos dentro del
ecosistema SGIVU. Orquesta la información proveniente de los servicios de clientes, usuarios y
vehículos para asegurar que cada transacción registre datos consistentes (participantes, valores,
condiciones de pago y estado del contrato) y expone APIs REST para su administración completa.

## 🧱 Arquitectura y Rol

* Tipo: Microservicio Spring Boot / Spring Cloud enfocado en la gestión del ciclo de contratos.
* Interactúa con: `sgivu-config`, `sgivu-discovery`, `sgivu-gateway`, `sgivu-auth`, `sgivu-client`,
  `sgivu-user` y `sgivu-vehicle`.
* Valida referencias externas (cliente, usuario, vehículo) antes de crear o actualizar un contrato,
  consultando a los microservicios correspondientes mediante `RestClient` y claves internas.
* Persiste el historial de contratos con precios, términos, restricciones y observaciones en
  PostgreSQL (`purchase_sales` + secuencia `purchase_sales_id_seq`).
* Servicio expuesto vía Eureka y protegido en el gateway; soporta paginación, filtros por actor y
  eliminación lógica por identificador.

## ⚙️ Tecnologías

* **Lenguaje:** Java 21 (Amazon Corretto)
* **Framework:** Spring Boot 3.5.7, Spring Cloud 2025.0.0
* **Seguridad:** OAuth 2.1 Resource Server, JWT (claim `rolesAndPermissions`), autorización con
  `@PreAuthorize`, `InternalServiceAuthorizationManager`
* **Persistencia:** Spring Data JPA, PostgreSQL, scripts `schema.sql` / `data.sql`
* **Integración:** Spring REST Client + `HttpServiceProxyFactory`, MapStruct, Jakarta Validation
* **Infraestructura:** Docker, Spring Boot Actuator, Eureka Client, Config Client
* **Utilitarios:** Lombok, Micrometer listo para Zipkin (vía configuración centralizada)

## 🚀 Ejecución Local

1. Posicionarse en `sgivu-purchase-sale` y compilar dependencias:

   ```bash
   ./mvnw clean package
   ```

2. Levantar dependencias requeridas:
   * Config Server (`sgivu-config`) con los perfiles del servicio.
   * Eureka Server (`sgivu-discovery`).
   * Authorization Server (`sgivu-auth`) para la emisión de JWT.
   * Microservicios `sgivu-client`, `sgivu-user`, `sgivu-vehicle` (expuestos en el gateway o
     directamente).
   * PostgreSQL con la base `sgivu_purchase_sale_db`. Ejecutar `database/schema.sql` y, si se desea,
     `database/data.sql` para datos de ejemplo.

3. Configurar variables o crear un `application-local.yml` con los parámetros mínimos:

   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/sgivu_purchase_sale_db
       username: sgivu
       password: sgivu
     jpa:
       hibernate:
         ddl-auto: none
     sql:
       init:
         mode: never # usar "always" solo para aplicar schema/data.sql automáticamente
   server:
     port: 8084
   eureka:
     client:
       service-url:
         defaultZone: http://localhost:8761/eureka
   services:
     map:
       sgivu-auth:
         name: sgivu-auth
         url: http://localhost:9000
       sgivu-client:
         name: sgivu-client
         url: http://localhost:8082
       sgivu-user:
         name: sgivu-user
         url: http://localhost:8081
       sgivu-vehicle:
         name: sgivu-vehicle
         url: http://localhost:8083
   service:
     internal:
       secret-key: ${SERVICE_INTERNAL_SECRET_KEY}
   management:
     tracing:
       enabled: true # opcional si se usa Zipkin
   ```

4. Ejecutar el microservicio:

   ```bash
   SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run
   ```

5. Acceder a las APIs en `http://localhost:8084` (o a través del gateway). La paginación usa tamaño
   fijo de 10 elementos.

## 🔗 Endpoints Principales

```text
POST   /v1/purchase-sales             -> Registra un contrato de compra/venta (valida cliente, usuario y vehículo).
GET    /v1/purchase-sales/{id}        -> Obtiene un contrato por ID.
GET    /v1/purchase-sales             -> Lista completa de contratos.
GET    /v1/purchase-sales/page/{page} -> Página de contratos (page size = 10).
PUT    /v1/purchase-sales/{id}        -> Actualiza precios, condiciones y vinculaciones.
DELETE /v1/purchase-sales/{id}        -> Elimina un contrato (hard delete).
GET    /v1/purchase-sales/client/{clientId}   -> Historial de contratos por cliente.
GET    /v1/purchase-sales/user/{userId}       -> Contratos gestionados por un usuario interno.
GET    /v1/purchase-sales/vehicle/{vehicleId} -> Contratos asociados a un vehículo.
GET    /actuator/health|info         -> Endpoints públicos de monitoreo.
```

* Todos los contratos devueltos utilizan `PurchaseSaleResponse` con detalles comerciales y términos.
* El endpoint paginado y los filtros aplican validaciones internas para resolver IDs de entidad.

## 🔐 Seguridad

* Resource Server que valida JWT emitidos por `sgivu-auth`; el claim `rolesAndPermissions` se convierte
  en `SimpleGrantedAuthority`.
* Reglas de autorización por método (`@PreAuthorize`) con permisos finos:
  `purchase_sale:create|read|update|delete`.
* `InternalServiceAuthorizationManager` permite llamadas internas enviando el header
  `X-Internal-Service-Key` con la clave configurada (`service.internal.secret-key`).
* `JwtAuthorizationInterceptor` copia automáticamente el token del contexto de seguridad hacia las
  llamadas salientes a `sgivu-client`, `sgivu-user` y `sgivu-vehicle`.
* `GET /actuator/health` y `GET /actuator/info` son públicos; el resto requiere token válido o clave
  interna.

## 🧩 Dependencias

* `sgivu-config`: configuración centralizada (datasource, URLs de servicios, tracing).
* `sgivu-discovery`: registro y descubrimiento de instancias (Eureka).
* `sgivu-gateway`: expone las APIs al exterior y propaga headers internos.
* `sgivu-auth`: autenticación y emisión de tokens OAuth 2.1 / OpenID Connect.
* `sgivu-client`, `sgivu-user`, `sgivu-vehicle`: validación de entidades participantes en cada contrato.
* PostgreSQL: almacenamiento transaccional de contratos y estados contractuales.

## 🧮 Dockerización

* Imagen: `sgivu-purchase-sale` basada en `amazoncorretto:21-alpine-jdk`.
* Puerto expuesto: `8084/tcp`.
* Build & run de referencia:

  ```bash
  ./mvnw clean package -DskipTests
  docker build -t sgivu-purchase-sale .
  docker run --rm -p 8084:8084 \
    -e SPRING_PROFILES_ACTIVE=prod \
    -e SPRING_CONFIG_IMPORT=configserver:http://sgivu-config:8888 \
    -e SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/sgivu_purchase_sale_db \
    -e SPRING_DATASOURCE_USERNAME=sgivu \
    -e SPRING_DATASOURCE_PASSWORD=sgivu \
    -e SERVICE_INTERNAL_SECRET_KEY=... \
    -e EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://sgivu-discovery:8761/eureka \
    -e SERVICES_MAP_SGIVU_AUTH_URL=http://sgivu-auth:9000 \
    -e SERVICES_MAP_SGIVU_CLIENT_URL=http://sgivu-client:8082 \
    -e SERVICES_MAP_SGIVU_USER_URL=http://sgivu-user:8081 \
    -e SERVICES_MAP_SGIVU_VEHICLE_URL=http://sgivu-vehicle:8083 \
    sgivu-purchase-sale
  ```

## 🐳 Build & Push Docker

- Ejecuta `./build-image.bash` desde esta carpeta para detener/borrar contenedores previos, limpiar la imagen
  `stevenrq/sgivu-purchase-sale:v1`, empaquetar con Maven y publicar la nueva imagen (`docker build && docker push`).
- El orquestador `../build-docker-images/build_push_all.bash` llama automáticamente a este script al construir todos los
  servicios.

## ☁️ Despliegue en AWS

* Publicar la imagen en Amazon ECR y desplegar en ECS/Fargate, EKS o EC2 detrás de `sgivu-gateway`.
* Utilizar AWS RDS (PostgreSQL) y aplicar `schema.sql` durante el bootstrap o mediante migraciones.
* Gestionar secretos (`SERVICE_INTERNAL_SECRET_KEY`, `SPRING_DATASOURCE_*`, URLs internas) con
  Secrets Manager o Parameter Store.
* Configurar Auto Scaling y health checks apuntando a `/actuator/health`; exponer métricas a CloudWatch
  o Prometheus según la estrategia del clúster.
* Asegurar conectividad privada (VPC/Subnets) hacia `sgivu-config`, `sgivu-discovery`, `sgivu-auth` y
  los microservicios dependientes.

## 📐 Diagramas
- Contexto del servicio: `docs/architecture/services/sgivu-purchase-sale-context.puml`
- Componentes internos (clientes REST, interceptores, repositorio): `docs/architecture/services/sgivu-purchase-sale-components.puml`
- Modelo de datos (PurchaseSale + enums): `docs/architecture/datamodel/sgivu-purchase-sale-datamodel.puml`

## 📊 Monitoreo

* Spring Boot Actuator habilita `health`, `info`, `metrics` y `prometheus` (según configuración).
* logs estructurados listos para agregarse en CloudWatch, ELK o Loki.
* Integración con Micrometer/Zipkin configurable vía Config Server (`management.tracing.*`,
  `management.zipkin.tracing.endpoint`).

## ✨ Autor

* **Steven Ricardo Quiñones**
