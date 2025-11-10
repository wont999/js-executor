# appliner-internship

1. Запустить docker kafka
2. Запустить discovery server
3. Запустить gateway server
4. Запустить routing server
5. Запустить клиентов server


Пример запроса:
```
curl -X POST http://localhost:8080/routing/api/procedures/execute
  -H "Content-Type: application/json"
  -H "Authorization: Bearer ${token}"
  -d '{
    "clientType": "client-1",
    "procedureName": "calculateSum",
    "parameters": {
      "a": 22215,
      "b": 25
    }
  }'
```