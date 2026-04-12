# Inventory Sync API

A middleware microservice that orchestrates product data from two external providers, standardizes it into a canonical model, persists it, and exposes it through a REST API with dynamic filtering capabilities.

---

## Key Features

* Scheduled inventory synchronization every 10 minutes
* Parallel data ingestion using CompletableFuture
* Graceful degradation when a provider fails
* Canonical data model normalization
* Idempotent persistence to prevent duplicates
* Dynamic filtering at database level (JPA Specifications)
* Bulk stock update endpoint using database-level operations

---

## Architecture Overview

```
┌─────────────────┐     ┌─────────────────┐
│  FakeStore API  │     │  DummyJSON API  │
│ (Provider A)    │     │  (Provider B)   │
└────────┬────────┘     └────────┬────────┘
         │                       │
         └──────────┬────────────┘
                    │ Parallel calls (CompletableFuture)
                    ▼
         ┌─────────────────────┐
         │   InventoryService  │
         │  - Homologation     │
         │  - Cron Job (10min) │
         │  - Idempotent Sync  │
         └──────────┬──────────┘
                    │
                    ▼
         ┌─────────────────────┐
         │     PostgreSQL      │
         │  (products table)   │
         └──────────┬──────────┘
                    │
                    ▼
         ┌─────────────────────┐
         │   REST API          │
         │  GET  /inventory    │
         │  PATCH /restock     │
         │  POST  /sync        │
         └─────────────────────┘
```

---

## Tech Stack

* Java 21
* Spring Boot 3.3.5
* Spring Data JPA (Hibernate)
* PostgreSQL (production) / H2 (local)
* Docker & Docker Compose
* Lombok
* JUnit 5 & Mockito

---

## Running the Project with Docker

### Prerequisites

* Docker installed and running

### Steps

```bash
git clone https://github.com/jesus9611/inventory-sync-api.git
cd inventory-sync-api
docker compose up --build
```

API available at:

http://localhost:8080

---

## API Endpoints

### GET /api/v1/inventory

Returns all products with optional filters.

| Parameter | Type    | Description            |
| --------- | ------- | ---------------------- |
| minRating | Double  | Minimum rating         |
| maxPrice  | Double  | Maximum price          |
| minStock  | Integer | Minimum stock          |
| provider  | String  | ProviderA or ProviderB |

Example:

```bash
curl "http://localhost:8080/api/v1/inventory?minRating=4.0&maxPrice=50&provider=ProviderB"
```

---

### PATCH /api/v1/inventory/restock-zeros

Updates all products with stock = 0 using a database-level bulk operation.

```bash
curl -X PATCH http://localhost:8080/api/v1/inventory/restock-zeros \
  -H "Content-Type: application/json" \
  -d '{"newStock": 10}'
```

---

### POST /api/v1/inventory/sync

Triggers manual sync.

```bash
curl -X POST http://localhost:8080/api/v1/inventory/sync
```

---

## How to Test the API

You can test the API using curl or Postman.

Example workflow:

1. Trigger a manual sync:

```bash
curl -X POST http://localhost:8080/api/v1/inventory/sync
```

2. Retrieve products:

```bash
curl http://localhost:8080/api/v1/inventory
```

3. Apply filters:

```bash
curl "http://localhost:8080/api/v1/inventory?minRating=4.0"
```

4. Restock products with zero stock:

```bash
curl -X PATCH http://localhost:8080/api/v1/inventory/restock-zeros \
  -H "Content-Type: application/json" \
  -d '{"newStock": 10}'
```

---

## Technical Decisions

### Idempotent Sync Strategy

The synchronization process ensures no duplicate records by using a unique internalId.

* Existing products → updated
* New products → inserted

This ensures the sync process is safe to run multiple times without side effects.

---

### Dynamic Filtering (JPA Specifications)

Filtering is executed at database level using JpaSpecificationExecutor, avoiding in-memory processing.

---

### Parallel Processing

Used CompletableFuture to call providers concurrently, reducing execution time.

---

### Resilience (Graceful Degradation)

Failures are handled using exceptionally(), allowing the system to continue processing.

---

### Database Efficiency

Bulk updates (restock) use @Modifying queries directly in the database.

---

### Canonical ID Strategy

* Provider A → FS_{id}
* Provider B → DJ_{id}

---

### Audit Handling

Provider A does not provide stock:

* Default = 0
* auditStock = true
* Logged for traceability

---

## Assumptions

* External provider APIs may fail intermittently
* Product IDs are unique within each provider
* Data consistency is prioritized over real-time synchronization

---

## Design Principles

* Separation of concerns
* Resilient system design
* Idempotent data processing
* Scalable querying strategy
* Clean architecture

---

## Testing

```bash
mvn clean test
```

---

## Future Improvements

* Swagger / OpenAPI
* Redis caching
* CI/CD (GitHub Actions)
* Kafka
* Integration tests

---

## Project Structure

```
src/main/java/com/supplychain/homologator/inventorysyncapi/
├── client/
├── config/
├── controller/
├── domain/
├── dto/
├── exception/
├── repository/
└── service/
```
