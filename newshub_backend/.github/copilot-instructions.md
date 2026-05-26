# NewsHub GitHub Copilot Instructions

## Principles
1. **Database Efficiency**: NEVER add redundant database calls. Reuse existing entities from `CustomUserDetails`.
2. **Context Awareness**: Always check `CustomUserDetails` before making a DB query for the current user.
3. **Immutability & Safety**: Use Java 17 `record` for DTOs. Ensure all service methods are `@Transactional` when modifying state.
4. **Clean Code**: Follow the Controller-Service-Repository pattern. Keep controllers thin; business logic belongs in services.
5. **Unified Mapping**: All entity-to-DTO conversions MUST happen in `NewsHubMapper`.

## Coding Standards
- **Enums for State**: Use `UserRole` instead of `String` for roles.
- **Fail Fast**: Use custom exceptions (`ResourceNotFoundException`, `AccessDeniedException`, `BadRequestException`) with meaningful messages.
- **No Empty Responses**: Service methods that modify state should return the updated DTO to provide immediate feedback to the UI.

## Technical Skills
- **Spring Security**: Leverage `@AuthenticationPrincipal` to access the currently logged-in user without extra lookups.
- **JPA Optimizations**: Use `@Enumerated(EnumType.STRING)` for enums and avoid `EAGER` fetching unless strictly necessary.
- **WebSockets**: Trigger real-time updates via `NewsWebSocketController` for create/update/delete actions on news.

## Testing Strategy

### Unit Tests (Service Layer)
- Focus exclusively on **Service layer** classes: `AuthService`, `UserService`, `CategoryService`, `NewsService`
- Target coverage: **80-90%** of service methods
- Use Mockito for mocking all external dependencies
- Test class naming: `{ServiceName}Tests`
- Follow AAA pattern: Arrange → Act → Assert
- Test method naming: `test{MethodName}Success`, `test{MethodName}NotFound`, `test{MethodName}Exception`

### Integration Tests (Controller Layer)
- Cover **positive scenarios only** (happy paths)
- Test class naming: `{ControllerName}IntegrationTests`
- Use `@SpringBootTest` with `MockMvc`
- Validate HTTP status codes: 200, 201, 400, 401, 403, 404
- Use `@Transactional` to rollback test data
- Validate response content with `jsonPath`

### Test Coverage Report
Generate test coverage report using JaCoCo:
```bash
/home/arsen/apache-maven-3.9.14/bin/mvn clean test jacoco:report
```
View report at: `target/site/jacoco/index.html`

### Coverage Goals
| Component | Target |
|-----------|--------|
| Services | 80-90% |
| Controllers | 70% |
| Models | 60% |
| Exceptions | 80% |

## Maven Location
```
/home/arsen/apache-maven-3.9.14/bin/mvn
```

## Running Tests
```bash
/home/arsen/apache-maven-3.9.14/bin/mvn test                                    # All tests
/home/arsen/apache-maven-3.9.14/bin/mvn test -Dtest=UserServiceTests          # Specific test class
/home/arsen/apache-maven-3.9.14/bin/mvn test -Dtest=UserServiceTests#testName # Specific test method
```

## Comments 
- Do not use comments, NEVER
- Do not generate some other .md files

