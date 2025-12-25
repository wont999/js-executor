# Blockly Executor Service

Микросервис для безопасного выполнения Blockly/JS скриптов.

## Запуск

```bash
docker-compose up -d --build
```

Масштабирование:
```bash
docker-compose up -d --scale blockly-executor=3
```

## Тестовый запрос

1. Получить токен:
```bash
curl -X POST "http://localhost:8080/realms/appliner/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=gateway-client" \
  -d "client_secret=tI8E3gAt6NqYRD0GhsKBhEpV3sPWyqy0" \
  -d "username=testuser" \
  -d "password=testpass" \
  -d "grant_type=password"
```

2. Выполнить скрипт:
```bash
curl -X POST "http://localhost:8180/routing/api/procedures/execute" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN>" \
  -d '{
    "clientType": "blockly-executor",
    "procedureName": "executeBlocklyScript",
    "parameters": {
      "script": "var result = 2 + 2; result;",
      "parameters": {}
    }
  }'
```

## Структура

- `blockly-executor/` — основной сервис выполнения скриптов
- `common/` — общие модели и интерфейсы
- `routing/` — маршрутизация запросов через Kafka
- `gateway/` — API Gateway
- `discovery/` — Eureka Service Discovery
