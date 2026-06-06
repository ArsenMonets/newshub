# 📰 NewsHub — Платформа дистрибуції контенту

Сучасна вебплатформа, що побудована за архітектурним шаблоном розподіленого клієнт-серверного застосунку, призначена для публікації новин, гнучкого керування рольовим доступом користувачів та забезпечення інтерактивної взаємодії за допомогою трансляції подій у реальному часі.

---

## 👤 Інформація про автора

- **Виконавець:** Монець А.В.
- **Академічна група:** ФеП-32
- **Науковий керівник:** Жишкович А.В., асистент
- **Дата захисту / виконання:** 29.05.2026

---

## 🛠️ Специфікація технологічного стека

Проєкт реалізовано на базі виробничих технологій промислового рівня, які згруповано за компонентами системи:
- **Backend Core:** Java 17, Spring Boot (Spring Security + JWT, Spring Data JPA, Spring Web, Spring WebSockets)
- **Frontend Core:** TypeScript, Angular Framework (RxJS, TailwindCSS, Сomponent Architecture)
- **Data Tier:** PostgreSQL (Реляційна СКБД), Hibernate (ORM)
- **DevOps & Infrastructure:** Docker, Docker Compose (Оркестрація контейнерів), Multi-stage Docker Builds

---

## 📐 Архітектурне моделювання проекту

### 1. Функціональна модель: Діаграма прецедентів (Use Case Diagram)
Відображає права доступу користувачів (Гість, Читач, Автор, Адміністратор) та логіку обов'язкового розширення бізнес-прецедентів через сервіс автентифікації.

```mermaid
graph TD
    G[Гість]
    R[Читач]
    A[Автор]
    ADM[Адміністратор]

    R --> G
    A --> R
    ADM --> A

    UC1(Перегляд та фільтрація новин)
    UC2(Реєстрація у системі)
    UC3(Управління підписками)
    UC4(Публікація та редактура статей)
    UC5(Модерація контенту та блокування)
    UC6(Генерація та перевірка JWT)

    G --> UC1
    G --> UC2
    
    R --> UC3
    A --> UC4
    ADM --> UC5

    UC3 -.-> UC6
    UC4 -.-> UC6
    UC5 -.-> UC6

```

### 2. Структурна модель: Діаграма пакетів та патернів (Package Diagram)

Демонструє розшарування системи, застосування архітектурних патернів MVVM (на клієнті), MVC та Layered Architecture (на сервері), а також використання DTO для ізоляції сутностей бази даних.

```mermaid
graph LR
    subgraph newshub_frontend [newshub_frontend /src/app/]
        direction TB
        F_Comp[components]
        F_Serv[services]
        F_Api[api]
        F_Mod[models]
        
        F_Comp --> F_Serv
        F_Serv --> F_Api
        F_Serv --> F_WS
        F_Api --> F_Mod
        F_WS --> F_Mod
    end

    subgraph newshub_backend [newshub_backend /src/main/java/...]
        direction TB
        B_Ctrl[controllers]
        B_WS[websocket]
        B_Serv[services]
        B_Repo[repositories]
        B_Mod[models]
        B_DTO[dto]
        
        B_Ctrl --> B_Serv
        B_Ctrl --> B_DTO
        B_Serv --> B_Repo
        B_Serv --> B_DTO
        B_Repo --> B_Mod
    end

    subgraph Database_Tier [database]
        DB[(PostgreSQL)]
    end

    %% Мережеві зв'язки (Протоколи взаємодії)
    F_Api -.->|HTTP REST| B_Ctrl
    F_Api -.->|WS / STOMP| B_WS
    B_Repo -.->|JDBC / SQL| DB
```

### 3. Топологічна модель: Діаграма розгортання (Deployment Diagram)

Описує фізичне середовище виконання проєкту та мережеві порти взаємодії між ізольованими Docker-контейнерами.

```mermaid
graph TD
    subgraph Client_Node [Вузол клієнта: Браузер]
        App[Angular Web Application]
    end

    subgraph Server_Node [Серверний вузол: Docker Engine]
        Web[Контейнер фронтенду: NGINX <br> Порт: 4200]
        API[Контейнер бекенду: Spring Boot <br> Порт: 8080]
        DB[(Контейнер БД: PostgreSQL <br> Порт: 5432)]
    end

    App -- HTTP / Static Files --> Web
    App -- REST API: HTTP / JSON --> API
    App -- Real-time: WebSockets / STOMP --> API
    API -- JDBC / SQL --> DB

```

---

## 📂 Структурна організація монорепозиторію

```
NewsHub/
├── newshub_backend/          # Модуль серверної бізнес-логіки (Spring Boot)
│   ├── src/main/java         # Вихідний код компонентів (Controller, Service, Repository)
│   ├── src/main/resources    # Конфігураційні файли (application.yml)
│   ├── pom.xml               # Декларація залежностей збірки Maven
│   └── Dockerfile            # Інструкція багатоетапної збірки (JDK 17)
├── newshub_frontend/         # Модуль клієнтського інтерфейсу (Angular)
│   ├── src/app               # Модулі, компоненти та сервіси застосунку
│   ├── package.json          # Конфігурація npm-пакетів та скриптів збірки
│   └── Dockerfile            # Скрипт компіляції та розгортання под NGINX
├── compose.yaml              # Головний файл оркестрації Docker Compose
├── Makefile                  # Набір CLI-аліасів для розробника
└── README.md                 # Технічна документація системи

```

---

## 🔑 Безпека та змінні оточення (.env)

Для безпечного розгортання системи в колі проєкту перед запуском створюється файл `.env` із наступними ключовими параметрами конфігурації:

```properties
POSTGRES_DB=newshub_db
POSTGRES_USER=postgres_admin
POSTGRES_PASSWORD=secure_password_2026
JWT_SECRET=super_secret_cryptographic_key_for_newshub_application

```

---

## 🚀 Швидкий запуск інфраструктури

### Системні вимоги до хост-машини

* Наявність встановленого демона **Docker** (v20.10 або новіша)
* Інструмент утиліти **Docker Compose** (v2.0 або новіша)

### Алгоритм розгортання

```bash
# Крок 1. Клонування репозиторію проекту з GitHub
git clone [https://github.com/your-user/NewsHub.git](https://github.com/your-user/NewsHub.git)
cd NewsHub

# Крок 2. Компіляція вихідного коду та побудова Docker-образів
docker-compose build

# Крок 3. Запуск усіх сервісів в ізольованому фоновому режимі (detached)
docker-compose up -d

```

### Мережеві адреси для перевірки працездатності

* **Клієнтський UI (Angular + NGINX):** http://localhost:4200
* **Серверний API (Spring Boot REST):** http://localhost:8080
* **Системна база даних (PostgreSQL):** `localhost:5432`

---

## 🛑 Сервісні команди зупинки та очищення

```bash
# Зупинка роботи застосунку зі збереженням стану бази даних
docker-compose down

# Зупинка системи із повним видаленням баз даних (очищення томиків)
docker-compose down -v

# Комплексне видалення контейнерів, застарілих мереж та локальних образів
docker-compose down -v --rmi all --remove-orphans

```

---

## 🛠️ Використання автоматизації через `make`

Утиліта `make` дозволяє欴 оптимізувати повсякденну роботу за допомогою вбудованих інструкцій з Makefile:

```bash
make up       # Швидке розгортання та запуск інфраструктури
make logs     # Безперервний вивід логів (stdout) усіх контейнерів у консоль
make down     # Безпечне вимкнення серверного стеку
make restart  # Швидкий перезапуск усіх модулів платформи
make clean    # Повне системне очищення Docker від слідів проекту

```
