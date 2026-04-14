# Shipping Service

Сервис упаковки посылок на Spring Boot с тремя интерфейсами:

- REST API (`/api/parcels`, `/api/commands/*`);
- Spring Shell (консольные команды);
- Telegram-бот.

Проект использует:

- Spring Data JPA + PostgreSQL;
- Flyway для миграций;
- OpenAPI/Swagger для документации;
- Docker Compose для локального окружения.

## Что умеет

- CRUD посылок по имени;
- пагинация списка посылок;
- 4 алгоритма упаковки: `simple`, `optimized`, `even`, `dense`;
- команды погрузки/выгрузки через REST и Shell;
- Telegram-команды для основных операций.

## Технологии

- Java 18
- Spring Boot 3.3.5
- Spring Web
- Spring Data JPA
- Spring Shell
- Flyway
- springdoc-openapi
- PostgreSQL 16

## Быстрый старт

### Локально (без Docker)

1. Подними PostgreSQL и создай БД `shipping`.
2. Укажи параметры подключения через env-переменные:

```powershell
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/shipping"
$env:SPRING_DATASOURCE_USERNAME="shipping"
$env:SPRING_DATASOURCE_PASSWORD="shipping"
```

3. Запусти приложение:

```powershell
.\gradlew.bat bootRun
```

Flyway применит миграции автоматически при старте.

### Через Docker Compose

В корне проекта:

```powershell
docker compose up --build
```

Это поднимет:

- `postgres` — база данных;
- `app` — REST + Shell;
- `bot` — Telegram-бот (если заданы переменные).

Остановить:

```powershell
docker compose down
```

Сбросить БД (вместе с volume):

```powershell
docker compose down -v
```

## Переменные окружения

### Для приложения

- `SPRING_DATASOURCE_URL` (по умолчанию `jdbc:postgresql://localhost:5432/shipping`)
- `SPRING_DATASOURCE_USERNAME` (по умолчанию `shipping`)
- `SPRING_DATASOURCE_PASSWORD` (по умолчанию `shipping`)

### Для Telegram-бота

- `TELEGRAM_BOT_TOKEN` — обязательно
- `TELEGRAM_ALLOWED_CHAT_ID` — опционально, но рекомендуется

### `.env` для Docker Compose

Создай файл `.env` рядом с `docker-compose.yml`:

```env
TELEGRAM_BOT_TOKEN=123456:abc...
TELEGRAM_ALLOWED_CHAT_ID=123456789
```

## Где смотреть API

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/api-docs`

## REST API (основные эндпоинты)

### Посылки

- `POST /api/parcels` — создать посылку
- `GET /api/parcels/{name}` — получить по имени
- `DELETE /api/parcels/{name}` — удалить
- `GET /api/parcels?page=0&size=20` — список с пагинацией

### Команды

- `POST /api/commands/pack`
- `POST /api/commands/load`
- `POST /api/commands/unload`

## Примеры `curl`

### Создать посылку

```bash
curl -X POST "http://localhost:8080/api/parcels" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "test4x4",
    "shape": ["oooo", "o  o", "o  o", "oooo"]
  }'
```

### Список посылок с пагинацией

```bash
curl "http://localhost:8080/api/parcels?page=0&size=10"
```

### Pack-команда

```bash
curl -X POST "http://localhost:8080/api/commands/pack" \
  -H "Content-Type: application/json" \
  -d '{
    "parcelNames": ["test3x3", "test3x2"],
    "truckWidth": 6,
    "truckHeight": 6,
    "algorithm": "dense",
    "maxTrucks": 10
  }'
```

## Spring Shell команды

После старта приложения доступны:

- `createpackage --name <name> --form <form>`
- `findpackage --name <name>`
- `deletepackage --name <name>`
- `listpackages --page 0 --size 20`
- `pack --parcels <a,b> --truckWidth <w> --truckHeight <h> --algorithm <code> --maxTrucks <n>`
- `load --parcels <a,b> --trucks "<w>x<h> <w>x<h>" --algorithm <code>`
- `unload --withCount true|false`

## Telegram-бот

Сервис `bot` в `docker-compose.yml` запускает `TelegramShippingBotApp`.

Запуск только бота:

```powershell
docker compose up --build bot
```

Запуск вместе с API и БД:

```powershell
docker compose up --build
```

## Flyway и данные в БД

- миграции: `src/main/resources/db/migration`
- при старте приложения выполняются автоматически

Проверить примененные миграции:

```powershell
docker compose exec postgres psql -U shipping -d shipping -c "select * from flyway_schema_history order by installed_rank;"
```

Проверить посылки:

```powershell
docker compose exec postgres psql -U shipping -d shipping -c "select name,width,height from parcels;"
```

## Тесты и сборка

```powershell
.\gradlew.bat test
.\gradlew.bat clean bootJar
```
