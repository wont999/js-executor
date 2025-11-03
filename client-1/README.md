# Client-1 Service - Microservice Architecture

Этот сервис является worker-сервисом для обработки процедур типа CLIENT_1 в микросервисной архитектуре с использованием Kafka и Eureka Discovery.

## Архитектура

```
Discovery Gateway (Eureka Server) :8761
    ↓ (регистрация)
Client-1 Service :8081
    ↓ (слушает)
Kafka Topic: client-1-procedures
    ↓ (отвечает в)
Kafka Topic: routing-responses-{instanceId}
    ↓
Routing Service :8080
```

## Функциональность

### Обрабатываемые процедуры:

1. **calculateSum** - вычисление суммы двух чисел
   ```json
   {
     "procedureName": "calculateSum",
     "parameters": { "a": 10, "b": 20 }
   }
   ```

2. **calculateProduct** - вычисление произведения двух чисел
   ```json
   {
     "procedureName": "calculateProduct",
     "parameters": { "a": 5, "b": 7 }
   }
   ```

3. **processOrder** - обработка заказа
   ```json
   {
     "procedureName": "processOrder",
     "parameters": { "orderId": "ORD-12345", "action": "approve" }
   }
   ```

4. **getUserInfo** - получение информации о пользователе
   ```json
   {
     "procedureName": "getUserInfo",
     "parameters": { "userId": 12345 }
   }
   ```

5. **healthCheck** - проверка состояния сервиса
   ```json
   {
     "procedureName": "healthCheck",
     "parameters": {}
   }
   ```

## Конфигурация

### Eureka Client
- **Service Name**: `CLIENT-1`
- **Port**: 8081
- **Eureka Server**: http://localhost:8761/eureka/

### Kafka
- **Bootstrap Servers**: localhost:29092
- **Consumer Topic**: `client-1-procedures`
- **Consumer Group**: `client-1-worker-group`
- **Producer**: Отправляет ответы в reply-to топик

## Запуск

### Предварительные требования:
1. Запущен **Kafka** на порту 29092
2. Запущен **Discovery Gateway** на порту 8761
3. Java 17+

### Запуск сервиса:

```bash
cd /home/k1mb/home/java/disc_get_test/client-1
./gradlew bootRun
```

### Проверка работоспособности:

1. **Проверка регистрации в Eureka:**
   ```
   http://localhost:8761
   ```
   Должен быть виден сервис `CLIENT-1`

2. **Health Check:**
   ```bash
   curl http://localhost:8081/actuator/health
   ```

3. **Eureka Info:**
   ```bash
   curl http://localhost:8081/actuator/eureka
   ```

## Интеграция с Routing Service

Routing Service автоматически проверяет доступность CLIENT-1 через Eureka перед отправкой запросов в Kafka.

### Пример запроса через Routing Service:

```bash
curl -X POST http://localhost:8080/api/procedures/execute \
  -H "Content-Type: application/json" \
  -d '{
    "clientType": "CLIENT_1",
    "procedureName": "calculateSum",
    "parameters": {
      "a": 15,
      "b": 25
    }
  }'
```

## Логирование

Логи включают:
- Получение запросов из Kafka
- Выполнение процедур
- Отправку ответов
- Регистрацию в Eureka
- Ошибки и исключения

## Мониторинг

Доступные endpoints через Actuator:
- `/actuator/health` - статус здоровья
- `/actuator/info` - информация о сервисе
- `/actuator/metrics` - метрики
- `/actuator/eureka` - статус Eureka клиента

## Масштабирование

Сервис поддерживает горизонтальное масштабирование:
- Каждый экземпляр регистрируется в Eureka с уникальным instance-id
- Kafka Consumer Group обеспечивает распределение нагрузки
- Routing Service видит все доступные экземпляры через Eureka

### Запуск дополнительных экземпляров:

```bash
# Экземпляр 2 (порт 8082)
SERVER_PORT=8082 ./gradlew bootRun

# Экземпляр 3 (порт 8083)
SERVER_PORT=8083 ./gradlew bootRun
```

## Troubleshooting

### Сервис не регистрируется в Eureka
- Проверьте, что Discovery Gateway запущен на порту 8761
- Проверьте логи на наличие ошибок подключения
- Убедитесь, что `eureka.client.register-with-eureka=true`

### Не обрабатываются сообщения из Kafka
- Проверьте, что Kafka запущен и доступен
- Проверьте настройки consumer group
- Проверьте логи на наличие ошибок десериализации

### Routing Service не видит сервис
- Убедитесь, что имя сервиса `CLIENT-1` (в верхнем регистре)
- Проверьте статус в Eureka Dashboard
- Дождитесь обновления регистрации (может занять до 30 секунд)
