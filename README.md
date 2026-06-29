# match-me graphql

match-me graphql is a GraphQL implementation on top of the match-me backend that provides all of REST capability excluding subscriptions with GraphQL.

## Tech Stack

- Backend: Java 25, Spring Boot 4, Spring Security, JWT, WebSocket/STOMP, JPA/Hibernate, GraphQL
- Database: PostgreSQL with PostGIS
- Realtime: Spring WebSocket message broker with STOMP client

## Requirements

For Docker setup:
- Docker Desktop or Docker Engine with Docker Compose

For manual setup:
- Java 25
- Node.js and npm
- PostgreSQL with PostGIS extension
- OpenSSL, for generating local JWT keys

## Docker Setup

The quickest way to run the app locally is Docker Compose. It starts:

- PostgreSQL 17 with PostGIS
- Spring Boot backend on `http://localhost:8080`

From the repository root:

### Development Profile
```bash
docker compose -f docker-compose.yml -f docker-compose.dev.yml up --build
```

Then open:

```text
http://localhost:3000
```

The backend runs with the `dev` profile in Docker, so GraphQL playground is 
available at `http://localhost:8080/graphiql` and the review seeder is available at: `http://localhost:8080/seeder`

Seeded profile emails can be viewed in the database, their password is always `Password123!`
```sql
psql -U graphql
SELECT * FROM users;
```

### Production Profile
```bash
docker compose -f docker-compose.yml -f docker-compose.prod.yml up --build
```

Useful Docker commands:

```bash
docker compose up --build
docker compose down
docker compose down -v
```

## Manual Setup

### 1. Create databases

Create one database for development and one for backend integration tests. Both need PostGIS enabled.

```bash
createdb matchme
createdb matchme_test

psql -d matchme -c "CREATE EXTENSION IF NOT EXISTS postgis;"
psql -d matchme_test -c "CREATE EXTENSION IF NOT EXISTS postgis;"
```

The current local backend config expects:

```text
database: matchme
test database: matchme_test
username: postgres
password: 8ded6076
```

Adjust `match-me-backend/src/main/resources/application.properties` and `match-me-backend/src/test/resources/application-test.properties` if your local PostgreSQL credentials differ.

### 2. Generate JWT keys

From the repository root, create the key directory expected by the backend config:

```bash
mkdir -p ./JWT-keys
openssl genpkey -algorithm RSA -out ./JWT-keys/private.key -pkeyopt rsa_keygen_bits:2048
openssl rsa -pubout -in ./JWT-keys/private.key -out ./JWT-keys/public.key
```

### 3. Start backend

Use the `dev` profile when you want the review seeder page at `/seeder`.

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Backend runs at:

```text
http://localhost:8080
```

## Review Data Seeder

When the backend is running with the `dev` profile, open:

```text
http://localhost:8080/seeder
```

Seeder options:

- `0 users`: clears users, profiles, bios, locations, chats, connections, recommendations, and uploaded avatars
- `20 users`: loads a small curated dataset
- `200 users`: loads a larger fictitious dataset for recommendation testing

Seeded users use:

```text
Password123!
```

Demo accounts from the 20-user dataset:

| Email | Email | Email | Email |
| --- | --- | --- | --- |
| `liisa@example.com` | `mart@example.com` | `ragnar@example.com` | `anna@example.com` |
| `kristjan@example.com` | `laura@example.com` | `kertu@example.com` | `marko@example.com` |
| `alex@example.com` | `grete@example.com` | `henry@example.com` | `marleen@example.com` |
| `otto@example.com` | `eva@example.com` | `raul@example.com` | `tiina@example.com` |
| `janar@example.com` | `sandra@example.com` | `denis@example.com` | `katri@example.com` |

The 200-user dataset generates fictitious users with `@example.com` emails and the same password.

## Usage Guide

When testing in GraphQL playground, after logging in copy the token and paste it into `Headers` section
```bash
{
  "Authorization": "Bearer <token>"
}
```

1. Register or log in.
2. Complete the profile page, including city/location preference.
3. Complete the bio quiz.
4. Open Discovery to view recommendations.
5. Dismiss recommendations or send connection requests.
6. Open Friends to view connections, incoming requests or sent requests.
7. Accept an incoming request from another user or cancel sent requests.
8. Open the connected user's profile or start/resume a chat (message), or cancel connection (remove).
9. From chats page start/resume chat with connected users.

Recommendations are based on:

- archetype score compatibility calculated from bio quiz answers
- mutual radius-based location filtering
- dismissed recommendations
- existing pending or accepted connections

The algorithm intentionally derives compatibility from user answers instead of asking users to manually choose a target archetype.

## Notes for Reviewers

- For controlled review data, run the project locally with the `dev` profile and use `/seeder`.
- Profile data is intentionally not searchable.
- Email is private and is not returned by public user/profile/bio endpoints.
- Chat is only available between accepted connections.
- Realtime chat uses WebSocket/STOMP rather than polling.