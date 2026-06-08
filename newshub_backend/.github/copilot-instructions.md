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
- **Spring Security**: Leverage `@AuthenticationPrincipal` to access the currently logged-in user without extra lookups.
- **JPA Optimizations**: Use `@Enumerated(EnumType.STRING)` for enums and avoid `EAGER` fetching unless strictly necessary.
- **WebSockets**: Trigger real-time updates via `NewsWebSocketController` for create/update/delete actions on news.

## Comments 
- Do not use comments, NEVER
- Do not generate some other .md files

