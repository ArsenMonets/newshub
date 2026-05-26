.PHONY: help build up down restart logs logs-backend logs-frontend logs-db clean rebuild

help:
	@echo "NewsHub Docker Compose Commands"
	@echo "=============================="
	@echo "make build          - Build all Docker images"
	@echo "make up             - Start all services (db, backend, frontend)"
	@echo "make down           - Stop all services"
	@echo "make restart        - Restart all services"
	@echo "make logs           - Show logs from all services"
	@echo "make logs-backend   - Show backend logs"
	@echo "make logs-frontend  - Show frontend logs"
	@echo "make logs-db        - Show database logs"
	@echo "make clean          - Stop services and remove volumes"
	@echo "make rebuild        - Rebuild images and restart services"
	@echo "make shell-db       - Connect to database shell"
	@echo "make shell-backend  - Connect to backend container"

# Build all images
build:
	docker-compose build

# Start all services
up:
	docker-compose up -d
	@echo "✓ All services started"
	@echo "Frontend:  http://localhost:4200"
	@echo "Backend:   http://localhost:8080"
	@echo "Database:  localhost:5432"

# Start all services and follow logs
up-logs:
	docker-compose up

# Stop all services
down:
	docker-compose down
	@echo "✓ All services stopped"

# Restart all services
restart: down up

# Show logs from all services
logs:
	docker-compose logs -f

# Show backend logs
logs-backend:
	docker-compose logs -f backend

# Show frontend logs
logs-frontend:
	docker-compose logs -f frontend

# Show database logs
logs-db:
	docker-compose logs -f postgres

# Stop services and remove volumes (clean slate)
clean:
	docker-compose down -v
	@echo "✓ All services stopped and volumes removed"

# Rebuild images and restart
rebuild: clean build up
	@echo "✓ Rebuild completed"

# Connect to PostgreSQL shell
shell-db:
	docker-compose exec postgres psql -U postgres -d newshub

# Connect to backend container
shell-backend:
	docker-compose exec backend /bin/bash

# Health check
health:
	@echo "Checking service health..."
	@docker-compose ps
	@echo ""
	@echo "Backend health: $$(curl -s -o /dev/null -w '%{http_code}' http://localhost:8080/actuator/health || echo 'not available')"
