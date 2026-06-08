---
name: testing-strategy
description: Use this skill to enforce the testing strategy, verify test coverage goals, write TS unit/integration tests, and run npm test commands.
---

## Testing Strategy

Whenever you implement or modify a feature, you **must** write corresponding tests according to these explicit rules:

### **API Layer (`api/`)**

* **Do not** write any unit tests (integration you should) for the `api` package.

### **Unit Tests**

* **Target:** Primarily written for classes inside the `services/` directory.
* **Coverage:** Cover **all possible scenarios**, including edge cases, error handling, boundary conditions, and alternative flows.

### **Integration Tests**

* **Target:** Written for the entire system interaction (connecting components, services, etc.).
* **Coverage:** Write **only Happy Paths**. Do not test edge cases or failures at this level.

---