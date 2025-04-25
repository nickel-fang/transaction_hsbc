# Bank Transactions API

A RESTful API (v2) for managing financial transactions with Spring Boot.

## Features
- Create, update, delete transactions
- Get a transaction or list transactions with pagination
- Caching for improved performance
- Docker support

## Requirements
- Java 21 or later
- Maven
- Docker (optional)

## Build & Run

### terminal
```bash
mvn clean test   (optional)
mvn clean package -Dmaven.test.skip=true
java -jar target/transaction-api-0.0.1-SNAPSHOT.jar
````

### Docker
```bash
docker build -t hsbc/transaction-api .
docker run -d -p 8080:8080 hsbc/transaction-api
```

### Kubernetes
```bash
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
```

## API Endpoints
- POST /api/v2/transactions - Create a transaction
- PUT /api/v2/transactions/{id} - Update a transaction
- DELETE /api/v2/transactions/{id} - Delete a transaction
- GET /api/v2/transactions/{id} - Get a transaction
- GET /api/v2/transactions - List transactions (supports page and size params)

## Project Dependencies
- spring-boot-starter-web: Provides Spring MVC and embedded Tomcat server for REST API development
- spring-boot-starter-validation: Enables bean validation (e.g., `@NotNull`, `@Positive`) for request input validation
- spring-boot-starter-cache: Spring's cache abstraction layer for unified caching operations
- caffeine: High-performance local caching library used as the cache implementation
- lombok: Reduces boilerplate code via annotations (auto-generates getters/setters/constructors)
- spring-boot-starter-actuator: Provides production-ready monitoring endpoints (health checks, metrics, etc.)
- spring-boot-starter-test: Testing framework (JUnit 5, Mockito, etc.) for unit/integration tests

## demo
Start the transaction API service, you can demo the APIs in terminal. Also you can use some tools like Postman or Insomnia.
### create transaction
```bash
curl --location 'http://localhost:8080/api/v2/transactions' \
--header 'Content-Type: application/json' \
--data '{
    "referenceNumber": "HSBC-TX-000001",
    "amount": 88.88,
    "currency": "CNY",
    "type": "TRANSFER",
    "senderAccount": "1111111111111111",
    "receiverAccount": "2222222222222222",
    "beneficiaryName": "Nickel Fang",
    "channel": "WeChat",
    "status": "PENDING",
    "description": null,
    "ipAddress": "192.168.1.1",
    "deviceFingerprint": "fingerprint"
}'
```

you will get the response like following
```bash
{"id":7321404234614837248,"amount":88.88,"currency":"CNY","type":"TRANSFER","senderAccount":"1111111111111111","receiverAccount":"2222222222222222","beneficiaryName":"Nickel Fang","channel":"WeChat","status":"PENDING","description":null,"transactionTime":"2025-04-25T13:26:34.645404"}
```

If you send the same request in one minute, you will get the transaction duplicated error.
```bash
Transaction duplicated with TransactionRequest{senderAccount='1111111111111111', receiverAccount='2222222222222222', amount=88.88, currency='CNY'}
```

### query transaction
#### query all transaction by pagination
It make sense to start page from 1
```bash
curl --location 'http://localhost:8080/api/v2/transactions?page=1&size=10'
```

#### query one existing transaction
please replace the id (`7321404234614837248`) to the id created by the API of `create transaction`
```bash
curl --location 'http://localhost:8080/api/v2/transactions/7321404234614837248'
```

#### query a non-existing transaction
```bash
curl --location 'http://localhost:8080/api/v2/transactions/0'
```

you will get the transaction not found error
```bash
Transaction not found with id: 0
```

### update transaction
please replace the id (`7321404234614837248`) to the id created by the API of `create transaction`, and change some information of transaction request if needed.
```bash
curl --location --request PUT 'http://localhost:8080/api/v2/transactions/7321404234614837248' \
--header 'Content-Type: application/json' \
--data '{
    "referenceNumber": "HSBC-TX-000001",
    "amount": 99.99,
    "currency": "CNY",
    "type": "TRANSFER",
    "senderAccount": "1111111111111111",
    "receiverAccount": "2222222222222222",
    "beneficiaryName": "Nickel Fang",
    "channel": "WeChat",
    "status": "PENDING",
    "description": null,
    "ipAddress": "192.168.1.1",
    "deviceFingerprint": "fingerprint"
}'
```

if the transaction id is not existing, you will get the same error of transaction not found as querying a non-existing transaction.

### delete transaction
please replace the id (`7321404234614837248`) to the id created by the API of `create transaction`
```bash
curl --location --request DELETE 'http://localhost:8080/api/v2/transactions/7321404234614837248'
```

if the transaction id is not existing, you will get the same error of transaction not found as querying a non-existing transaction.
