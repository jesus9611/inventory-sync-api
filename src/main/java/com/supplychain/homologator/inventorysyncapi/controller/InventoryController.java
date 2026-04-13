package com.supplychain.homologator.inventorysyncapi.controller;

import com.supplychain.homologator.inventorysyncapi.dto.ProductFilter;
import com.supplychain.homologator.inventorysyncapi.dto.ProductResponseDTO;
import com.supplychain.homologator.inventorysyncapi.dto.RestockRequest;
import com.supplychain.homologator.inventorysyncapi.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "Endpoints for querying and managing the homologated product inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    @Operation(
        summary = "Query inventory with optional filters",
        description = "Returns all homologated products from the local database. Filters are applied at query level, not in memory."
    )
    @GetMapping
    public ResponseEntity<List<ProductResponseDTO>> getInventory(
            @Parameter(description = "Minimum rating threshold") @RequestParam(required = false) Double minRating,
            @Parameter(description = "Maximum price threshold") @RequestParam(required = false) Double maxPrice,
            @Parameter(description = "Minimum stock threshold") @RequestParam(required = false) Integer minStock,
            @Parameter(description = "Provider name: ProviderA or ProviderB") @RequestParam(required = false) String provider
    ) {
        log.info("GET /api/v1/inventory - filters: minRating={}, maxPrice={}, minStock={}, provider={}",
                minRating, maxPrice, minStock, provider);

        ProductFilter filter = new ProductFilter(minRating, maxPrice, minStock, provider);
        List<ProductResponseDTO> products = inventoryService.getProducts(filter);
        return ResponseEntity.ok(products);
    }

    @Operation(
        summary = "Bulk restock of zero-stock products",
        description = "Finds all products with stock = 0 and updates them to the provided newStock value. Uses a single PATCH query for efficiency."
    )
    @PatchMapping("/restock-zeros")
    public ResponseEntity<Map<String, Object>> restockZeros(
            @RequestBody RestockRequest request
    ) {
        Integer newStock = request.newStock();

       log.info("PATCH /api/v1/inventory/restock-zeros - newStock={}", newStock);

        int updated = inventoryService.restockZeros(newStock);

        return ResponseEntity.ok(Map.of(
                "message", "Stock updated successfully",
                "productsUpdated", updated,
                "newStockValue", newStock
        ));
    }

    @Operation(
        summary = "Trigger manual inventory sync",
        description = "Forces an immediate sync from FakeStore and DummyJSON in parallel, outside the scheduled 10-minute cron job."
    )
    @PostMapping("/sync")
    public ResponseEntity<Map<String, String>> triggerSync() {
        log.info("Manual sync triggered");
        inventoryService.syncInventory();
        return ResponseEntity.ok(Map.of("message", "Sync completed successfully"));
    }
}