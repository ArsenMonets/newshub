---
name: backend-architecture
description: Use this skill to design the Spring Boot backend architecture, enforce layered folder structures, separate Entity/DTO layers, and manage the Postgres database layer.
---

# Backend Architecture Skill

## Purpose

This skill enforces a unified, scalable, and secure Spring Boot backend architecture across the project, ensuring a strict separation of concerns, robust exception handling, and consistent data mapping.

## When to Use This Skill

* Triggered when creating or modifying backend components (Controllers, Services, Repositories).
* Triggered when designing new database schemas, Entities, or Data Transfer Objects (DTOs).
* Triggered when setting up cross-cutting concerns like global exceptions or security configurations.

## Step-by-Step Workflows

### 1. Project Structure & Layering

Ensure the backend source tree strictly adheres to the following Layered Architecture layout under `src/main/java/com/newshub/`:

* `config/`: Global configurations (Security, CORS, Database).
* `controller/`: REST controllers exposing API endpoints. Handles validation only.
* `service/`: Pure business logic interfaces and implementations (`impl/`).
* `repository/`: Spring Data JPA interfaces interacting with PostgreSQL.
* `model/`: Database `@Entity` classes representing the schema.
* `dto/`: Immutable **Java Records** for request payloads and response bodies.
* `exception/`: Global exception handlers (`@ControllerAdvice`) and custom domain exceptions.

### 2. Data Separation & Mapping Rules

* **No Leaking Entities**: Never expose database entities directly to controllers or clients.
* **DTO Usage**: Input payloads (e.g., `UserRegistrationRequest`) and output responses (e.g., `NewsResponse`) must be isolated using Java Records.
* **Validation**: Annotate DTOs with Jakarta Validation constraints (`@NotBlank`, `@Size`, `@Email`). Force validation in controllers using `@Valid`.

### 3. API Design & Error Handling

* **REST Best Practices**: Explicitly map HTTP methods (`GET`, `POST`, `PUT`, `DELETE`). Return specific HTTP status codes (200 OK, 201 Created, 400 Bad Request, 404 Not Found).
* **Global Interception**: Catch domain exceptions globally using an `@ExceptionHandler` framework to return standard error payloads containing a timestamp, status code, and clear error message.

## Executable Commands

### Compile and Build Backend

```bash
/home/arsen/apache-maven-3.9.14/bin/mvn clean package -DskipTests

```

### Run Backend Locally (Development Profile)

```bash
/home/arsen/apache-maven-3.9.14/bin/mvn spring-boot:run

```

### Build Backend Docker Image

```bash
docker build -t newshub-backend:latest ./newshub_backend

```

## Gotchas

* **Critical**: Do not inject repositories directly into controllers. All data operations must pass through the service layer.
* **Circular Dependencies**: Avoid cross-injecting services. If Service A needs Service B and vice versa, abstract the shared behavior or use an intermediate service.

---