# GitHub Copilot / AI Assistant Instructions for Angular Project

## 1. General Constraints & Code Style

* **No Comments:** Do not write any single-line (`//`) or multi-line (`/* */`) comments in the code. Code must be self-explanatory through clean naming conventions.
* **Efficiency:** Do not generate unnecessary reports, markdown summaries, or verbose explanations after completing a task. Keep responses concise to save tokens.

---

## 2. Project Architecture & Directory Structure

You must respect the following non-standard project structure. Do not attempt to "correct" or alter it:

* `api/` – Classes responsible strictly for server communication (HTTP requests, API clients).
* `services/` – Component-level classes handling business logic and state management (Component Services).
* `components/` – Pure HTML templates (and related layout files) that bind to the services.
* `models/` – Interfaces and types for objects transferred across the system.

---

## 3. Dependency Management

* Before suggesting or adding any new external dependencies or libraries, you **must** verify their status.
* **Strict Rule:** Do not propose or use any deprecated packages or features. Ensure they are actively maintained and compatible with the current environment.

---

## 4. Output Format

When asked to write code:

1. Provide the exact code implementation directly.
2. Provide the corresponding test file immediately after.
3. No comments in code. No token-wasting summaries.