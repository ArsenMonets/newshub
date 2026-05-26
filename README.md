#  NewsHub

Веб-застосунок для перегляду, публікації та управління новинами.

---

## Автор

- **ПІБ**: Монець А.В. 
- **Група**: ФеП-32
- **Керівник**: Жишкович А.В., асистент
- **Дата виконання**: [дд.мм.рррр]

---

## Структура проєкту

```
NewsHub/
├── newshub_backend/          # Spring Boot backend (Java)
│   ├── src/
│   ├── pom.xml
│   ├── Dockerfile
│   └── ...
├── newshub_frontend/         # Angular frontend (TypeScript)
│   ├── src/
│   ├── package.json
│   ├── Dockerfile
│   └── ...
├── compose.yaml              # Docker Compose конфіг
├── Makefile                  # Команди для управління
└── README.md
```

---

## Як запустити проєкт

### Передумови

- Docker (версія 20.10+)
- Docker Compose (версія 2.0+)

### Кроки для запуску

```bash
# 1. Клонування репозиторію
git clone https://github.com/your-user/NewsHub.git
cd NewsHub

# 2. Побудова образів
docker-compose build

# 3. Запуск всіх сервісів
docker-compose up -d
```

### Зупинення контейнерів

```bash
# Зупинити сервіси (зберегти дані)
docker-compose down

# Зупинити сервіси та видалити дані (томи)
docker-compose down -v
```

### Видалення контейнерів та образів

```bash
# Видалити контейнери, томи та образи
docker-compose down -v --rmi all

# Видалити orphan контейнери
docker-compose down -v --remove-orphans
```

### Доступ до сервісів

- **Frontend (Angular)**: http://localhost:4200
- **Backend (Spring Boot)**: http://localhost:8080
- **Database (PostgreSQL)**: localhost:5432

---

## Корисні команди (за наявності утиліти make)

```bash
# Запустити сервіси
make up

# Показати логи
make logs

# Зупинити сервіси
make down

# Перезавантажити сервіси
make restart

# Полна очистка
make clean
```
