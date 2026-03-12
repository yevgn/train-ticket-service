# API для сервиса покупки ЖД билетов

## Описание

Микросервис для:
- создания / изменения рейсов
- бронирования мест
- покупки билетов

## Технологии

- Java 17
- Spring Boot
- Spring Data JPA
- Spring Security
- JWT
- PostgreSQL
- Apache Kafka
- Swagger / OpenAPI

## Запуск проекта

### 1. Запуск БД
Запустить PostgreSQL (Docker или локально)

### 2. Запуск Kafka
Запустить брокер Kafka на localhost:9092 или выполнить команду docker-compose up (docker-compose.yml в корне проекта 
с kafka и zookeeper)

### 3. Запуск приложения (Docker или локально)