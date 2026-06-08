---
name: testing-strategy
description: Use this skill to enforce the testing strategy, verify test coverage goals, write Java unit/integration tests, and run Maven test commands.
---

# Testing Strategy Skill

## Purpose
This skill enforces a unified testing architecture across the project, ensures correct naming conventions, and provides exact commands for running tests and generating coverage reports.

## When to Use This Skill
* Triggered when writing or modifying Spring Boot tests.
* Triggered when validating test class/method naming standards.
* Triggered when a user needs to run tests or check JaCoCo coverage goals.

## Step-by-Step Workflows

### 1. Unit Tests (Service Layer)
Ensure all new or modified service tests adhere to these strict rules:
* **Focus**: Exclusively target `AuthService`, `UserService`, `CategoryService`, and `NewsService`.
* **Mocking**: Use **Mockito** to mock all external dependencies.
* **Pattern**: Always follow the **AAA** pattern (Arrange → Act → Assert).
* **Class Naming**: Must match `{ServiceName}Tests` (e.g., `UserServiceTests`).
* **Method Naming**: Use exactly these formats:
  * `test{MethodName}Success`
  * `test{MethodName}NotFound`
  * `test{MethodName}Exception`

### 2. Integration Tests (Controller Layer)
Ensure all controller tests adhere to these strict rules:
* **Scope**: Cover **positive scenarios only** (happy paths).
* **Setup**: Use `@SpringBootTest` with `MockMvc`.
* **Data Safety**: Always use `@Transactional` to automatically rollback test data.
* **Class Naming**: Must match `{ControllerName}IntegrationTests`.
* **Assertions**: Validate response content using `jsonPath` and verify correct HTTP status codes (200, 201, 400, 401, 403, 404).

### 3. Coverage Validation
When generating or reviewing test coverage, verify the metrics against these project goals:
* **Services**: 80-90% target coverage.
* **Controllers**: 70% target coverage.
* **Exceptions**: 80% target coverage.
* **Models**: 60% target coverage.

## Executable Commands

### Run All Tests
```bash
/home/arsen/apache-maven-3.9.14/bin/mvn test
```

### Run Specific Test Class
```bash
/home/arsen/apache-maven-3.9.14/bin/mvn test -Dtest=UserServiceTests
```

### Run Specific Test Method
```bash
/home/arsen/apache-maven-3.9.14/bin/mvn test -Dtest=UserServiceTests#testName
```

### Generate JaCoCo Coverage Report
```bash
/home/arsen/apache-maven-3.9.14/bin/mvn clean test jacoco:report
```
*Note: The generated report can be viewed locally at: `target/site/jacoco/index.html`*

## Gotchas
* **Critical**: Do not include integration tests or cover negative scenarios in the Service layer tests. Keep unit tests isolated.
* **Path Warning**: Always use the absolute Maven binary path `/home/arsen/apache-maven-3.9.14/bin/mvn` to avoid environment version mismatches.
