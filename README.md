# Wayfinder Backend

This repository contains the backend for **Wayfinder**, a trip planning web application.

The backend is built with **Java Spring Boot** and **PostgreSQL**, providing:

- REST API endpoints for cities, attractions, routes, and user data
- Backend caching to minimize external API calls
- Route optimization service for efficient trip planning

---

## Frontend

The frontend is maintained in a separate repository:
[Wayfinder Frontend](https://github.com/mayskii/wayfinder-frontend)

---

## Configuration

Settings come from environment variables (see `src/main/resources/application.properties`):

| Variable | Example | Default |
| --- | --- | --- |
| `DATABASE_URL` | `jdbc:postgresql://<host>/<db>?sslmode=require` | `jdbc:postgresql://localhost:5432/wayfinder` |
| `DATABASE_USERNAME` | `postgres` | `postgres` |
| `DATABASE_PASSWORD` | | `postgres` |
| `LOCATIONIQ_API_KEY` | your LocationIQ key | none, required |
| `PORT` | `10000` | `8080` |

Tables are created automatically on first start.

---

## Run locally

1. Create a PostgreSQL database named `wayfinder`.
2. Set `LOCATIONIQ_API_KEY` and start the server:

```bash
export LOCATIONIQ_API_KEY=your-key
mvn spring-boot:run
```

Or with Docker:

```bash
docker build -t wayfinder-backend .
docker run -p 8080:8080 -e DATABASE_URL=... -e DATABASE_USERNAME=... -e DATABASE_PASSWORD=... -e LOCATIONIQ_API_KEY=... wayfinder-backend
```

---

## Deployment

Deployed as a Docker web service on Render, with the database on Neon (PostgreSQL). On the free plan the server may sleep when idle, so the first request can take up to two minutes.

---

## Dependencies

- Spring Boot
- Spring Data JPA (Hibernate)
- PostgreSQL
- Maven
