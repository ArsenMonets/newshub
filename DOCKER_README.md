# Docker Compose Setup for NewsHub

Цей файл розповідає як запустити весь стек NewsHub (база даних, бекенд, фронтенд) за допомогою Docker Compose.

## Передумови

- Docker (версія 20.10+)
- Docker Compose (версія 2.0+)

## Швидкий старт

### 1. Побудова образів (перший запуск)
```bash
docker-compose build
```

### 2. Запуск всіх сервісів
```bash
docker-compose up -d
```

### 3. Доступ до сервісів
- **Frontend (Angular)**: http://localhost:4200
- **Backend (Spring Boot)**: http://localhost:8080
- **Database (PostgreSQL)**: localhost:5432

## Використання Makefile (рекомендується)

Для зручності використовуйте Makefile команди:

```bash
# Показати всі доступні команди
make help

# Запустити сервіси
make up

# Показати логи
make logs

# Перезавантажити сервіси
make restart

# Зупинити сервіси
make down

# Повна очистка (видалення волюмів)
make clean
```

## Структура сервісів

### PostgreSQL (postgres:16-alpine)
- **Контейнер**: newshub_db
- **Порт**: 5432
- **База даних**: newshub
- **Користувач**: postgres
- **Пароль**: mysecretpassword
- **Волюм**: postgres_data (збереження даних)

### Backend (Spring Boot 4.0.6)
- **Контейнер**: newshub_backend
- **Порт**: 8080
- **Профіль**: dev
- **Залежить від**: PostgreSQL

### Frontend (Angular)
- **Контейнер**: newshub_frontend
- **Порт**: 4200 (nginx)
- **Залежить від**: Backend

## Корисні команди

### Переглядання логів
```bash
# Всі логи
docker-compose logs -f

# Тільки бекенд
docker-compose logs -f backend

# Тільки фронтенд
docker-compose logs -f frontend

# Тільки база даних
docker-compose logs -f postgres
```

### Підключення до контейнерів
```bash
# PostgreSQL shell
docker-compose exec postgres psql -U postgres -d newshub

# Backend container shell
docker-compose exec backend /bin/bash

# Frontend container shell
docker-compose exec frontend /bin/sh
```

### Статус сервісів
```bash
docker-compose ps
```

### Перезапуск сервісу
```bash
docker-compose restart backend
docker-compose restart frontend
docker-compose restart postgres
```

## Змінні оточення

Всі змінні оточення зберігаються у файлі `.env`:

- **POSTGRES_DB**: Назва бази даних
- **POSTGRES_USER**: Користувач PostgreSQL
- **POSTGRES_PASSWORD**: Пароль PostgreSQL
- **SPRING_PROFILES_ACTIVE**: Spring профіль (dev/prod)
- **JWT_SECRET**: Секретний ключ для JWT
- **JWT_EXPIRATION**: Час закінчення JWT токену (мс)
- **ADMIN_LOGIN**: Логін адміністратора
- **ADMIN_PASSWORD**: Пароль адміністратора

## Розробка

### Першого запуску
```bash
# 1. Побудуйте образи
make build

# 2. Запустіть сервіси
make up

# 3. Переглядайте логи
make logs
```

### Під час розробки
```bash
# Переглядайте логи в реальному часі
make logs-backend

# Перезавантажте контейнер при змінах
docker-compose restart backend
```

### Очистка
```bash
# Зупинити сервіси
make down

# Видалити все (включно з даними)
make clean
```

## Усунення проблем

### Помилка: "Cannot connect to Docker daemon"
Переконайтесь, що Docker запущений:
```bash
docker ps
```

### Помилка: "Port 5432 already in use"
Змініть порт у `compose.yaml` або зупиніть інший PostgreSQL:
```bash
sudo service postgresql stop
```

### Помилка: "Connection refused"
Переконайтесь, що служба PostgreSQL готова:
```bash
docker-compose logs postgres
```

### Перебудова образів
```bash
docker-compose down
docker-compose build --no-cache
docker-compose up -d
```

## Мережа

Усі сервіси з'єднані через спеціальну мережу `newshub_network`, що дозволяє їм спілкуватися за назвою контейнера:
- Backend використовує `postgres:5432` для БД
- Frontend використовує `backend:8080` для API

## Додаткові ресурси

- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Angular Documentation](https://angular.io/docs)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
