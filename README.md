# Inventory Sync API

A middleware microservice that orchestrates product data from two external providers, standardizes it into a canonical model, persists it, and exposes it through a REST API with dynamic filtering capabilities.

---

## Key Features

* Scheduled inventory synchronization every 10 minutes
* Parallel data ingestion using CompletableFuture
* Graceful degradation when a provider fails
* Canonical data model normalization
* Dynamic filtering at database level (JPA Specifications)
* Bulk stock update endpoint

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

* **Java 21**
* **Spring Boot 3.3.5**
* **Spring Data JPA (Hibernate)**
* **PostgreSQL** (production) / **H2** (local)
* **Docker & Docker Compose**
* **Lombok**
* **JUnit 5 & Mockito**

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

```
http://localhost:8080
```

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

Updates all products with stock = 0

```bash
curl -X PATCH http://localhost:8080/api/v1/inventory/restock-zeros \
  -H "Content-Type: application/json" \
  -d '{"newStock": 10}'
```

---

### POST /api/v1/inventory/sync

Triggers manual sync

```bash
curl -X POST http://localhost:8080/api/v1/inventory/sync
```

---

## Technical Decisions

### Dynamic Filtering (JPA Specifications)

Filtering is executed at database level using `JpaSpecificationExecutor` to avoid loading unnecessary data into memory.

### Parallel Processing

Used `CompletableFuture` to call providers concurrently, improving performance and reducing total sync time.

### Resilience (Graceful Degradation)

If one provider fails, the system continues processing the available data source.

### Client Abstraction

Each provider has its own client class, ensuring separation of concerns and testability.

### Canonical ID Strategy

* Provider A → `FS_{id}`
* Provider B → `DJ_{id}`

Prevents ID collisions.

### Audit Stock Handling

Provider A has no stock → defaults to `0` and is flagged with `auditStock = true`.

---

## Design Principles

* Separation of concerns
* Resilient system design
* Scalable querying strategy
* Clean architecture with DTO/domain separation

---

## Testing

Run tests:

```bash
mvn clean test
```

Includes unit tests using Mockito for service layer validation.

---

## Future Improvements

* Swagger / OpenAPI documentation
* Redis caching
* CI/CD pipeline (GitHub Actions)
* Kafka event-driven architecture
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
