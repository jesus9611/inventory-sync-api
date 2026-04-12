package com.supplychain.homologator.inventorysyncapi.controller;

import com.supplychain.homologator.inventorysyncapi.dto.ProductFilter;
import com.supplychain.homologator.inventorysyncapi.dto.ProductResponseDTO;
import com.supplychain.homologator.inventorysyncapi.service.InventoryService;
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
public class InventoryController {

    private final InventoryService inventoryService;
   
    @GetMapping
    public ResponseEntity<List<ProductResponseDTO>> getInventory(
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Integer minStock,
            @RequestParam(required = false) String provider
    ) {
        log.info("GET /api/v1/inventory - filters: minRating={}, maxPrice={}, minStock={}, provider={}",
                minRating, maxPrice, minStock, provider);
       
        ProductFilter filter = new ProductFilter(minRating, maxPrice, minStock, provider);

        List<ProductResponseDTO> products = inventoryService.getProducts(filter);

        return ResponseEntity.ok(products);
    }

    @PatchMapping("/restock-zeros")
    public ResponseEntity<Map<String, Object>> restockZeros(
            @RequestBody Map<String, Integer> body
    ) {
        Integer newStock = body.get("newStock");

        if (newStock == null || newStock < 0) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                        "error", "newStock must be a positive number"
                    ));
        }

        log.info("PATCH /api/v1/inventory/restock-zeros - newStock={}", newStock);

        int updated = inventoryService.restockZeros(newStock);

        return ResponseEntity.ok(Map.of(
                "message", "Stock updated successfully",
                "productsUpdated", updated,
                "newStockValue", newStock
        ));
    }
   
    @PostMapping("/sync")
    public ResponseEntity<Map<String, String>> triggerSync() {
        log.info("Manual sync triggered");
        inventoryService.syncInventory();
        return ResponseEntity.ok(Map.of("message", "Sync completed successfully"));
    }
}