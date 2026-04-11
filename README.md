# Inventory Sync API

A middleware microservice that orchestrates product data from two external providers, standardizes it into a canonical model, persists it, and exposes it through a REST API with dynamic filtering capabilities.

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

- **Java 21**
- **Spring Boot 3.3.5**
- **Spring Data JPA** with Hibernate
- **PostgreSQL** (production) / **H2** (local development)
- **Docker & Docker Compose**
- **Lombok**

---

## Running the Project with Docker

### Prerequisites
- Docker installed and running

### Steps

```bash
# 1. Clone the repository
git clone https://github.com/jesus9611/inventory-sync-api.git
cd inventory-sync-api

# 2. Build and start the containers
docker compose up --build

# 3. The API is now available at:
# http://localhost:8080
```

### Trigger a manual sync
```bash
curl -X POST http://localhost:8080/api/v1/inventory/sync
```

---

## API Endpoints

### GET /api/v1/inventory
Returns all products with optional filters.

| Parameter | Type | Description |
|-----------|------|-------------|
| minRating | Double | Minimum product rating |
| maxPrice | Double | Maximum product price |
| minStock | Integer | Minimum stock available |
| provider | String | "ProviderA" or "ProviderB" |

**Example:**
```bash
curl "http://localhost:8080/api/v1/inventory?minRating=4.0&maxPrice=50&provider=ProviderB"
```

---

### PATCH /api/v1/inventory/restock-zeros
Updates all products with stock = 0 to a new value.

```bash
curl -X PATCH http://localhost:8080/api/v1/inventory/restock-zeros \
  -H "Content-Type: application/json" \
  -d '{"newStock": 10}'
```

---

### POST /api/v1/inventory/sync
Manually triggers the inventory synchronization.

```bash
curl -X POST http://localhost:8080/api/v1/inventory/sync
```

---

## Technical Decisions

### Dynamic Filtering with JPA Specifications
Filters are applied at the database level using `JpaSpecificationExecutor` and `Specification<Product>`. This avoids loading all records into memory and filtering with Streams, which would be inefficient at scale.

### Parallel API Calls with CompletableFuture
Both providers are called simultaneously using `CompletableFuture.runAsync()`. If one provider fails, the other continues processing normally (Graceful Degradation). Total sync time equals the slowest provider, not the sum of both.

### Client Layer Separation
Each external provider has its own dedicated client class (`FakeStoreClient`, `DummyJsonClient`). This follows the Single Responsibility Principle and makes each client independently testable and replaceable.

### Multi-Profile Configuration
The project uses Spring profiles to separate local (H2) and production (PostgreSQL) configurations. No code changes are needed to switch environments — Docker automatically activates the `docker` profile.

### Canonical ID Strategy
To avoid ID collisions between providers, each product gets a prefixed internal ID:
- Provider A: `FS_{id}` (e.g., `FS_1`)
- Provider B: `DJ_{id}` (e.g., `DJ_42`)

### Audit Stock Flag
Provider A does not include stock information. When a product from Provider A is saved, its stock defaults to 0 and `auditStock` is set to `true`, marking it for review.

---

## Project Structure

```
src/main/java/com/supplychain/homologator/inventorysyncapi/
├── client/          # External API clients (FakeStore, DummyJSON)
├── config/          # Spring configuration (RestTemplate, Scheduling)
├── controller/      # REST endpoints
├── domain/          # JPA entities
├── dto/             # Records for data transfer
├── exception/       # Global exception handling
├── repository/      # JPA repositories and Specifications
└── service/         # Business logic and sync orchestration
```