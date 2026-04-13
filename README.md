# Inventory Sync API

A middleware microservice that orchestrates product data from two external providers, standardizes it into a canonical model, persists it, and exposes it through a REST API with dynamic filtering capabilities.

---

## Key Features

* Scheduled inventory synchronization every 10 minutes
* Parallel data ingestion using CompletableFuture with a dedicated thread pool
* Graceful degradation when a provider fails
* Canonical data model normalization
* Idempotent persistence to prevent duplicates
* Dynamic filtering at database level (JPA Specifications)
* Bulk stock update endpoint using database-level operations
* OpenAPI / Swagger UI documentation

---

## Architecture Overview
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

---

## Tech Stack

* Java 21
* Spring Boot 3.5.x
* Spring Data JPA (Hibernate)
* PostgreSQL (Docker) / H2 (local)
* Docker & Docker Compose
* Springdoc OpenAPI 2.8.x
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

API available at: `http://localhost:8080`

Swagger UI available at: `http://localhost:8080/swagger-ui/index.html`

---

## Quick Test (for recruiters)

Once the app is running with Docker, follow these steps to verify everything works:

**1. Trigger a manual sync to populate the database:**
```bash
curl -X POST http://localhost:8080/api/v1/inventory/sync
```
Expected response:
```json
{"message": "Sync completed successfully"}
```

**2. Query all products:**
```bash
curl http://localhost:8080/api/v1/inventory
```
Expected response: a JSON array with products from both ProviderA (FS_) and ProviderB (DJ_).

**3. Filter by provider and rating:**
```bash
curl "http://localhost:8080/api/v1/inventory?provider=ProviderA&minRating=3.5"
```

**4. Restock all zero-stock products:**
```bash
curl -X PATCH http://localhost:8080/api/v1/inventory/restock-zeros \
  -H "Content-Type: application/json" \
  -d '{"newStock": 10}'
```
Expected response:
```json
{"message": "Stock updated successfully", "productsUpdated": 20, "newStockValue": 10}
```

Or explore everything interactively via Swagger UI at `http://localhost:8080/swagger-ui/index.html`

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

```bash
curl "http://localhost:8080/api/v1/inventory?minRating=4.0&maxPrice=50&provider=ProviderB"
```

### PATCH /api/v1/inventory/restock-zeros

Updates all products with stock = 0 using a database-level bulk operation.

```bash
curl -X PATCH http://localhost:8080/api/v1/inventory/restock-zeros \
  -H "Content-Type: application/json" \
  -d '{"newStock": 10}'
```

### POST /api/v1/inventory/sync

Triggers a manual sync outside the scheduled cron job.

```bash
curl -X POST http://localhost:8080/api/v1/inventory/sync
```

---

## Technical Decisions

### Idempotent Sync Strategy

Each product gets a unique `internalId` (`FS_{id}` for ProviderA, `DJ_{id}` for ProviderB). On every sync, existing records are updated and new ones are inserted, making the process safe to run multiple times.

### Dynamic Filtering (JPA Specifications)

Filters are applied using `JpaSpecificationExecutor`, pushing all filtering logic to the database query. No in-memory Stream filtering.

### Parallel Processing with Dedicated Executor

Both providers are called concurrently using `CompletableFuture.runAsync()` with a fixed thread pool of 2 threads, avoiding contention with the common ForkJoinPool.

### Resilience (Graceful Degradation)

Each provider call is wrapped with `.exceptionally()`, so if one provider fails the other continues processing normally.

### Bulk Restock Efficiency

The restock endpoint uses a `@Modifying` + `@Transactional` JPQL query, updating all zero-stock products in a single database operation instead of fetching and saving each one individually.

### Audit Handling

Provider A does not include stock data. The system defaults to 0 and sets `auditStock = true` for traceability.

---

## Assumptions

* External provider APIs may fail intermittently
* Product IDs are unique within each provider
* Data consistency is prioritized over real-time synchronization

---

## Testing

```bash
mvn clean test
```

---

## Future Improvements

* Redis caching for frequently queried filters
* CI/CD pipeline with GitHub Actions
* Integration tests with Testcontainers

---

## Project Structure
src/main/java/com/supplychain/homologator/inventorysyncapi/
├── client/
├── config/
├── controller/
├── domain/
├── dto/
├── exception/
├── repository/
└── service/