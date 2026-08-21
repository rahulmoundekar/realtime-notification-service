# Real-Time Notification Service

A production-oriented real-time notification microservice built with **Spring Boot**, **PostgreSQL**, **Redis Pub/Sub**, **WebSocket**, **Server-Sent Events (SSE)**, **JWT/Spring Security**, and an **Outbox pattern**.

The application provides durable notification storage through PostgreSQL and real-time delivery through WebSocket, with SSE available as a fallback. Redis Pub/Sub provides event fan-out so that notifications can be delivered correctly when multiple application instances are running.

---

## 1. What is this application?

This service is a **real-time notification microservice**.

It is designed for applications such as:

- E-commerce order-status notifications
- Payment and transaction notifications
- Account/security alerts
- Workflow and approval notifications
- Admin or operational alerts
- Any system that needs durable notifications plus real-time delivery

A notification is first persisted in PostgreSQL. An event is then made available for Redis Pub/Sub delivery. Connected clients can receive the event immediately through WebSocket or SSE.

The database remains the durable source for notification history and unread notifications, while Redis is used for transient real-time fan-out.

---

## 2. Main purpose

The service solves the common distributed real-time notification problem:

```text
Client connected to Server A

Notification created on Server B

        ↓

      Redis
        ↓

Server A receives the event

        ↓

WebSocket / SSE client receives notification
```

Without a shared event bus, Server B would not know that the user's active WebSocket/SSE connection exists on Server A.

Redis Pub/Sub provides the cross-instance fan-out mechanism.

---

## 3. Key capabilities

### REST APIs

- Create notification
- Get authenticated user's notifications
- Get authenticated user's unread notifications
- Mark a notification as read

### Real-time delivery

- WebSocket primary channel
- SSE fallback channel
- Connection registration and cleanup
- Heartbeat/reconnect support

### Distributed architecture

- Redis Pub/Sub
- Multi-instance WebSocket/SSE delivery
- Externalized/centralized connection awareness

### Security

- JWT-based authentication
- Spring Security Resource Server
- RSA public-key JWT verification
- User identity derived from JWT subject
- Notification ownership/authorization checks

### Reliability

- PostgreSQL persistence
- Transactional notification + outbox persistence
- Retryable outbox publishing
- Duplicate-event protection
- Offline-user recovery through notification history

### Operations

- Actuator health endpoint
- Metrics support
- Docker Compose setup
- Integration tests

---

# 4. High-level architecture

```text
                           Client
                             |
                    Authorization: Bearer JWT
                             |
              +--------------+--------------+
              |                             |
            REST                    WebSocket / SSE
              |                             |
              +--------------+--------------+
                             |
                    Spring Security
                             |
                     Authenticated User
                             |
                  Notification Service
                    /               \
                   /                 \
            PostgreSQL               Redis
                |                    Pub/Sub
                |                      |
                |             +--------+--------+
                |             |                 |
             Outbox      Instance 1        Instance 2
                |             |                 |
                |             +--------+--------+
                |                      |
                +---- durable ----  WebSocket/SSE
                     history            clients
```

---

# 5. Technology stack

| Technology | Purpose |
|---|---|
| Java 21 | Application runtime |
| Spring Boot 4.1.x | Application framework |
| Spring Web MVC | REST APIs |
| Spring WebSocket | WebSocket transport |
| Spring Data JPA | PostgreSQL persistence |
| PostgreSQL | Durable notification storage |
| Spring Data Redis | Redis access |
| Redis Pub/Sub | Cross-instance event fan-out |
| Spring Security | Authentication/authorization |
| JWT / RSA | Authentication token validation |
| Actuator | Health and metrics |
| Maven | Build and dependency management |
| Docker | Local infrastructure/application packaging |
| Docker Compose | Local multi-service environment |
| JUnit 5 | Automated testing |

---

# 6. Project structure

```text
src/
├── main/
│   ├── java/com/rahul/realtime/notification/
│   │   ├── config/
│   │   │   ├── SecurityConfig.java
│   │   │   ├── RedisConfig.java
│   │   │   └── NotificationMetrics.java
│   │   │
│   │   ├── controller/
│   │   │   └── ...
│   │   │
│   │   ├── dto/
│   │   │   ├── event/
│   │   │   ├── request/
│   │   │   └── response/
│   │   │
│   │   ├── entity/
│   │   │   ├── Notification.java
│   │   │   └── NotificationOutboxEvent.java
│   │   │
│   │   ├── enums/
│   │   │   └── ...
│   │   │
│   │   ├── exception/
│   │   │   └── ...
│   │   │
│   │   ├── repository/
│   │   │   ├── NotificationRepository.java
│   │   │   └── NotificationOutboxRepository.java
│   │   │
│   │   ├── redis/
│   │   │   └── RedisNotificationSubscriber.java
│   │   │
│   │   ├── security/
│   │   │   └── ...
│   │   │
│   │   ├── service/
│   │   │   ├── NotificationService.java
│   │   │   ├── NotificationPublisher.java
│   │   │   ├── NotificationDeliveryService.java
│   │   │   └── ...
│   │   │
│   │   ├── websocket/
│   │   │   └── ...
│   │   │
│   │   └── sse/
│   │       └── ...
│   │
│   └── resources/
│       ├── application.yml
│       └── security/
│           └── public-key.pem
│
└── test/
    ├── java/com/rahul/realtime/notification/
    │   ├── integration/
    │   └── support/
    └── resources/
        └── application-test.yml
```

---

# 7. Prerequisites for local development

Install:

1. **JDK 21**
2. **Maven 3.9+**
3. **Docker Desktop**
4. **Git**
5. **OpenSSL** for RSA key generation
6. IntelliJ IDEA or another Java IDE (optional)

Verify:

```powershell
java -version
mvn -version
docker version
```

Expected Java version:

```text
Java 21
```

---

# 8. Local infrastructure

The application needs:

```text
PostgreSQL → localhost:5432
Redis      → localhost:6379
```

The repository contains a `docker-compose.yml` that starts both services.

## Start infrastructure

From the project root:

```powershell
docker compose up -d
```

Check:

```powershell
docker compose ps
```

---

# 9. PostgreSQL configuration

The local Compose configuration uses:

```text
Database:  realtime_notification
Username:  postgres
Password:  root
Host:      localhost
Port:      5432
```

Verify:

```powershell
docker compose exec postgres pg_isready -U postgres -d realtime_notification
```

Expected:

```text
accepting connections
```

Verify the database:

```powershell
docker compose exec postgres psql -U postgres -d realtime_notification -c "SELECT current_database();"
```

---

# 10. Redis configuration

Local Redis:

```text
Host: localhost
Port: 6379
Channel: notification-events
```

Verify:

```powershell
docker compose exec redis redis-cli ping
```

Expected:

```text
PONG
```

The application property is:

```yaml
app:
  redis:
    notification-channel: notification-events
```

---

# 11. JWT and RSA security configuration

The notification service is a **JWT Resource Server**. It verifies tokens using an RSA public key.

Architecture:

```text
Authentication service
        |
        | signs JWT using private key
        v
      Client
        |
        | Authorization: Bearer <JWT>
        v
Notification Service
        |
        | validates signature using public key
        v
  authenticated user
```

The notification service should contain the **public key** only.

Do not commit a production private key to Git.

---

# 12. Creating a private/public RSA key pair locally

## Option A — OpenSSL from Git Bash / PowerShell

Create the directory if it does not exist:

```powershell
mkdir src\main\resources\security
```

Generate a 2048-bit RSA private key:

```powershell
openssl genrsa -out src\main\resources\security\private-key.pem 2048
```

Generate the X.509/SubjectPublicKeyInfo public key expected by the application:

```powershell
openssl rsa `
  -in src\main\resources\security\private-key.pem `
  -pubout `
  -out src\main\resources\security\public-key.pem
```

Verify the public key:

```powershell
Get-Content src\main\resources\security\public-key.pem
```

It must start and end with:

```text
-----BEGIN PUBLIC KEY-----
...
-----END PUBLIC KEY-----
```

A file starting with:

```text
-----BEGIN RSA PUBLIC KEY-----
```

is a different format and should not be used for the Spring Boot `public-key-location` configuration used in this project.

Validate it:

```powershell
openssl pkey -pubin -in src\main\resources\security\public-key.pem -text -noout
```

## Important security rule

For the notification-service Docker image and production deployment:

```text
public-key.pem   ✅ required
private-key.pem  ❌ do not package
```

The private key belongs to the service responsible for issuing/signing JWTs or to a secure key-management system.

---

# 13. Spring Security JWT configuration

`application.yml` uses the RSA public key from the classpath:

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          public-key-location: classpath:security/public-key.pem
```

The security configuration is stateless:

```text
REST request
   ↓
Bearer JWT
   ↓
JWT validation
   ↓
Authentication
   ↓
Authorization
```

---

# 14. Local application configuration

Typical local values are:

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/realtime_notification
    username: postgres
    password: root

  data:
    redis:
      host: localhost
      port: 6379
      timeout: 2s

app:
  redis:
    notification-channel: notification-events

  websocket:
    allowed-origins: http://localhost:3000,http://localhost:5173

  instance:
    id: ${INSTANCE_ID:${spring.application.name}-${server.port}}
```

When running inside Docker Compose, use service names instead of localhost:

```text
PostgreSQL host = postgres
Redis host      = redis
```

---

# 15. Build the application locally

From the project root:

```powershell
mvn clean package -DskipTests
```

The JAR will be under:

```text
target/
```

---

# 16. Run the application from IntelliJ

1. Start Docker Compose:

```powershell
docker compose up -d
```

2. Verify PostgreSQL and Redis are healthy.
3. Verify `public-key.pem` exists under:

```text
src/main/resources/security/public-key.pem
```

4. Run:

```text
RealtimeNotificationApplication
```

5. Verify:

```text
Tomcat started on port 8080
```

6. Health check:

```text
GET http://localhost:8080/actuator/health
```

---

# 17. Run from Windows CMD / PowerShell

## Start infrastructure

```powershell
docker compose up -d
```

## Build

```powershell
mvn clean package -DskipTests
```

## Run JAR

```powershell
java -jar target\realtime-notification-service-0.0.1-SNAPSHOT.jar
```

## Health check

PowerShell:

```powershell
curl.exe http://localhost:8080/actuator/health
```

---

# 18. Run the complete application using Docker Compose

Build the application JAR first:

```powershell
mvn clean package -DskipTests
```

Build the image:

```powershell
docker build -t realtime-notification-service:1.0 .
```

Start everything:

```powershell
docker compose up -d
```

Check:

```powershell
docker compose ps
```

View application logs:

```powershell
docker compose logs -f notification-service
```

Health:

```powershell
curl.exe http://localhost:8080/actuator/health
```

Stop everything:

```powershell
docker compose down
```

Stop and remove the PostgreSQL volume too:

```powershell
docker compose down -v
```

Use `down -v` carefully because it deletes the PostgreSQL Compose volume.

---

# 19. REST API contract

Base URL:

```text
http://localhost:8080
```

All protected APIs require:

```http
Authorization: Bearer <JWT>
```

## Create notification

```http
POST /api/v1/notifications
Content-Type: application/json
Authorization: Bearer <JWT>
```

Request:

```json
{
  "type": "ORDER_STATUS",
  "title": "Order Shipped",
  "message": "Your order #ORD-1001 has been shipped."
}
```

The user is taken from the authenticated JWT. The client does not choose the `userId`.

Example response:

```json
{
  "id": 1,
  "userId": "user-101",
  "type": "ORDER_STATUS",
  "title": "Order Shipped",
  "message": "Your order #ORD-1001 has been shipped.",
  "status": "UNREAD",
  "createdAt": "2026-08-21T10:00:00Z"
}
```

## Get current user's notifications

```http
GET /api/v1/notifications/me
Authorization: Bearer <JWT>
```

## Get current user's unread notifications

```http
GET /api/v1/notifications/me/unread
Authorization: Bearer <JWT>
```

## Mark as read

```http
PATCH /api/v1/notifications/{notificationId}/read
Authorization: Bearer <JWT>
```

The service verifies that the notification belongs to the authenticated user.

An attempt by another user returns:

```text
403 Forbidden
```

---

# 20. WebSocket usage

WebSocket endpoint:

```text
ws://localhost:8080/ws/notifications
```

The URL does **not** contain a user ID.

Identity comes from the JWT:

```text
JWT subject
    ↓
Principal.getName()
    ↓
userId
```

## Postman WebSocket test

1. Create or obtain a valid JWT.
2. Open a WebSocket request in Postman.
3. Connect to:

```text
ws://localhost:8080/ws/notifications
```

4. Add the handshake header:

```http
Authorization: Bearer <JWT>
```

5. Connect.

The server registers the session under the authenticated user.

When a notification is created for that user, the WebSocket receives the event.

Example event:

```json
{
  "notificationId": 12,
  "userId": "user-101",
  "type": "ORDER_STATUS",
  "title": "Order Shipped",
  "message": "Your order #ORD-1001 has been shipped.",
  "status": "UNREAD",
  "createdAt": "2026-08-21T10:00:00Z"
}
```

---

# 21. SSE usage

SSE endpoint:

```text
GET /api/v1/notifications/stream
```

Full URL:

```text
http://localhost:8080/api/v1/notifications/stream
```

Required header:

```http
Authorization: Bearer <JWT>
Accept: text/event-stream
```

## Using curl

```powershell
curl.exe -N `
  -H "Authorization: Bearer <JWT>" `
  -H "Accept: text/event-stream" `
  http://localhost:8080/api/v1/notifications/stream
```

`-N` prevents curl from buffering the stream so events are visible immediately.

A connection event may look like:

```text
event: connected
data: SSE connection established
```

Notification events are streamed when available.

---

# 22. Postman end-to-end test

A practical local test flow is:

## Step 1 — Health

```http
GET http://localhost:8080/actuator/health
```

Expected:

```json
{"status":"UP"}
```

## Step 2 — Generate/obtain JWT

Use your authentication service or your approved local development token mechanism.

Do not use the temporary development token endpoint in production.

## Step 3 — Connect WebSocket

```text
ws://localhost:8080/ws/notifications
```

Header:

```http
Authorization: Bearer <JWT>
```

## Step 4 — Create notification

```http
POST http://localhost:8080/api/v1/notifications
Authorization: Bearer <JWT>
Content-Type: application/json
```

Body:

```json
{
  "type": "ORDER_STATUS",
  "title": "Order Shipped",
  "message": "Your order #ORD-1001 has been shipped."
}
```

The same user's active WebSocket should receive the event.

## Step 5 — Check history

```http
GET http://localhost:8080/api/v1/notifications/me
Authorization: Bearer <JWT>
```

## Step 6 — Check unread

```http
GET http://localhost:8080/api/v1/notifications/me/unread
Authorization: Bearer <JWT>
```

## Step 7 — Mark as read

```http
PATCH http://localhost:8080/api/v1/notifications/{id}/read
Authorization: Bearer <JWT>
```

---

# 23. Running the multi-instance scenario locally

To simulate two application instances, run the application twice with different ports and instance IDs.

### Instance 1

```powershell
java -jar target\realtime-notification-service-0.0.1-SNAPSHOT.jar `
  --server.port=8080 `
  --app.instance.id=notification-service-8080
```

### Instance 2

Open another terminal:

```powershell
java -jar target\realtime-notification-service-0.0.1-SNAPSHOT.jar `
  --server.port=8081 `
  --app.instance.id=notification-service-8081
```

Both instances must point to the same:

```text
PostgreSQL
Redis
```

Example:

```text
Client WebSocket → localhost:8080

POST notification → localhost:8081

             ↓
           Redis
             ↓

WebSocket on :8080 receives notification
```

This is the key distributed behavior of the application.

---

# 24. Redis Pub/Sub flow

Channel:

```text
notification-events
```

Publisher:

```text
NotificationOutboxPublisher
        ↓
RedisNotificationPublisher
```

Subscriber:

```text
RedisNotificationSubscriber
        ↓
NotificationDeliveryService
```

Delivery:

```text
NotificationDeliveryService
       /                 \
WebSocket                 SSE
```

---

# 25. Outbox pattern

The outbox prevents the database/Redis consistency gap.

Instead of:

```text
DB save
   ↓
Redis publish
```

we persist both the notification and event record in one database transaction:

```text
Database transaction
      |
      +-- notification
      |
      +-- notification_outbox
```

Then:

```text
notification_outbox
        ↓
Outbox Publisher
        ↓
Redis Pub/Sub
```

If Redis is unavailable:

```text
notification       ✅ saved
outbox             ✅ saved
Redis publish      ❌ failed
```

When Redis becomes available again, the unpublished event can be retried.

---

# 26. Failure and recovery behavior

## PostgreSQL unavailable

Notification creation fails because persistence is the source of truth.

## Redis unavailable

The notification remains persisted and the outbox event remains pending for retry.

## WebSocket unavailable

The notification remains in PostgreSQL. The client can reconnect and read unread/history APIs.

## SSE unavailable

The notification remains in PostgreSQL and can be retrieved after reconnect.

## Instance restart

WebSocket/SSE clients reconnect and register again.

## Duplicate event

The delivery deduplicator prevents repeated delivery within an application instance.

---

# 27. Testing

Run all tests:

```powershell
mvn clean test
```

Important integration tests include:

```text
PostgreSqlConnectivityTest
RedisSubscriberIntegrationTest
NotificationServiceIntegrationTest
NotificationApiIntegrationTest
MultiInstanceApplicationLauncherTest
MultiInstanceNotificationIntegrationTest
MultiInstanceSseIntegrationTest
SseConnectionManagerTest
NotificationOutboxRecoveryIntegrationTest
NotificationDeliveryDeduplicatorTest
```

The most important distributed test is:

```text
MultiInstanceNotificationIntegrationTest
```

because it verifies:

```text
Instance 2
   ↓
Redis
   ↓
Instance 1
   ↓
WebSocket
```

---

# 28. Useful Docker commands

Check containers:

```powershell
docker compose ps
```

View all logs:

```powershell
docker compose logs -f
```

Application logs:

```powershell
docker compose logs -f notification-service
```

Redis logs:

```powershell
docker compose logs -f redis
```

PostgreSQL logs:

```powershell
docker compose logs -f postgres
```

Redis shell:

```powershell
docker compose exec redis redis-cli
```

Check Redis:

```text
PING
```

Expected:

```text
PONG
```

PostgreSQL shell:

```powershell
docker compose exec postgres psql -U postgres -d realtime_notification
```

Stop:

```powershell
docker compose down
```

---

# 29. Useful Maven commands

Compile:

```powershell
mvn clean compile
```

Run tests:

```powershell
mvn clean test
```

Build JAR:

```powershell
mvn clean package -DskipTests
```

Run application:

```powershell
mvn spring-boot:run
```

Run one test:

```powershell
mvn -Dtest=NotificationDeliveryDeduplicatorTest test
```

---

# 30. Troubleshooting

## `Public key location does not exist`

Verify:

```text
src/main/resources/security/public-key.pem
```

and:

```yaml
spring.security.oauth2.resourceserver.jwt.public-key-location: classpath:security/public-key.pem
```

## `Missing key encoding`

The public key should use:

```text
-----BEGIN PUBLIC KEY-----
```

not:

```text
-----BEGIN RSA PUBLIC KEY-----
```

## `Could not find a valid Docker environment`

The application itself does not require Testcontainers. Ensure Docker Desktop is running and use the project's Docker Compose setup:

```powershell
docker compose up -d
```

## PostgreSQL connection failure

Check:

```powershell
docker compose ps
docker compose exec postgres pg_isready -U postgres -d realtime_notification
```

## Redis connection failure

Check:

```powershell
docker compose exec redis redis-cli ping
```

Expected:

```text
PONG
```

## `401 Unauthorized`

Check:

```text
Authorization: Bearer <JWT>
```

and verify the token is valid for the configured public key.

## `403 Forbidden`

The authenticated user is not authorized to access the requested notification/resource.

## WebSocket connects but no notification is received

Check:

1. Redis is running.
2. `notification-events` channel is configured consistently.
3. The JWT subject matches the notification user.
4. The WebSocket is connected before creating the notification.
5. The application instance is subscribed to Redis.

## SSE connects but no event is received

Check:

1. `Accept: text/event-stream` is sent.
2. The JWT is valid.
3. The SSE stream endpoint is `/api/v1/notifications/stream`.
4. Redis subscriber and delivery service are running.
5. The client does not buffer the stream (for curl use `-N`).

---

# 31. Security recommendations for production

- Never commit a production private RSA key.
- Prefer an external authentication/authorization service for issuing JWTs.
- Keep only the public verification key in the notification service, or use a JWK/issuer-based configuration.
- Use HTTPS/WSS in production.
- Restrict WebSocket allowed origins to trusted frontend domains.
- Use strong PostgreSQL and Redis credentials.
- Do not expose Redis directly to the public Internet.
- Keep Actuator endpoints restricted in production.
- Configure structured logging and centralized log aggregation.
- Use a shared/distributed deduplication strategy when multiple instances require global duplicate suppression.

---

# 32. End-to-end local startup sequence

The simplest full local startup sequence is:

```powershell
# 1. Start infrastructure
docker compose up -d

# 2. Verify infrastructure
docker compose ps
docker compose exec postgres pg_isready -U postgres -d realtime_notification
docker compose exec redis redis-cli ping

# 3. Build application
mvn clean package -DskipTests

# 4. Run application locally
java -jar target\realtime-notification-service-0.0.1-SNAPSHOT.jar

# 5. Verify API
curl.exe http://localhost:8080/actuator/health
```

Then:

```text
1. Obtain a valid JWT
2. Connect WebSocket in Postman
3. POST a notification
4. Observe WebSocket event
5. GET /api/v1/notifications/me
6. GET /api/v1/notifications/me/unread
7. PATCH /api/v1/notifications/{id}/read
8. Test SSE with curl -N
```

---

# 33. What was implemented across the project steps

```text
01  Project architecture and API contract                 ✅
02  Spring Boot project setup                              ✅
03  PostgreSQL configuration + Notification entity         ✅
04  Notification creation API                              ✅
05  Notification history / pagination                      ✅
06  Mark notification as read                              ✅
07  Unread API + global exception handling                 ✅
08  Redis configuration + notification publisher           ✅
09  Redis subscriber + delivery service                    ✅
10  WebSocket implementation                               ✅
11  Distributed connection awareness                         ✅
12  SSE fallback                                            ✅
13  Multi-instance deployment/testing                       ✅
14  Cleanup / heartbeat / reconnect                         ✅
15  JWT / Spring Security                                   ✅
16  Notification ownership / authorization                  ✅
17  Failure handling + Outbox pattern                       ✅
18  Integration + multi-instance tests                      ✅
19  SSE integration + recovery tests                        ✅
20  Final hardening / duplicate protection / Docker         ✅
```

---

# 34. Learning outcomes / concepts demonstrated

This project demonstrates several practical backend and distributed-system concepts:

- REST API design
- Spring Boot microservice development
- PostgreSQL persistence
- JPA repositories and transactions
- Redis Pub/Sub
- WebSocket real-time communication
- SSE fallback communication
- JWT authentication
- Spring Security Resource Server
- Authorization and ownership enforcement
- Multi-instance application behavior
- Distributed event fan-out
- Outbox pattern
- At-least-once event publishing
- Duplicate-event protection
- Connection lifecycle management
- Heartbeat and reconnect handling
- Failure recovery
- Docker and Docker Compose
- Integration testing
- Actuator and metrics

---

# 35. Final architecture summary

```text
                       +-------------------+
                       |       Client      |
                       +---------+---------+
                                 |
                         JWT / Bearer Token
                                 |
                 +---------------v---------------+
                 |       Notification Service     |
                 |                                 |
                 | REST | WebSocket | SSE | JWT  |
                 +-------+---------------+---------+
                         |               |
                         |               |
                 +-------v-------+   +---v--------+
                 |  PostgreSQL   |   |    Redis    |
                 |               |   |   Pub/Sub   |
                 | Notification  |   +------+-------+
                 | Outbox        |          |
                 +-------+-------+      +---+---+
                         |              |       |
                         |         Instance A  Instance B
                         |              |       |
                         +--------------+-------+
                                        |
                                  WebSocket / SSE
```

The main design principle is:

```text
PostgreSQL = durable state
Redis      = real-time fan-out
WebSocket  = primary real-time delivery
SSE        = fallback delivery
JWT        = authenticated identity
Outbox     = reliable DB-to-event boundary
```

---

# 36. Project status

**API 07 — Real-Time Notification Service is implemented and locally runnable with PostgreSQL + Redis + Docker Compose.**

The project demonstrates a realistic distributed notification architecture with REST, WebSocket, SSE, Redis Pub/Sub, JWT security, PostgreSQL persistence, outbox-based recovery, multi-instance delivery, automated integration tests, and Docker-based local deployment.
