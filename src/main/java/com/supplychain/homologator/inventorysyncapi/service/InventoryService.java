package com.supplychain.homologator.inventorysyncapi.service;

import com.supplychain.homologator.inventorysyncapi.client.DummyJsonClient;
import com.supplychain.homologator.inventorysyncapi.client.FakeStoreClient;
import com.supplychain.homologator.inventorysyncapi.domain.Product;
import com.supplychain.homologator.inventorysyncapi.dto.DummyJSONProductDTO;
import com.supplychain.homologator.inventorysyncapi.dto.FakeStoreProductDTO;
import com.supplychain.homologator.inventorysyncapi.dto.ProductFilter;
import com.supplychain.homologator.inventorysyncapi.dto.ProductResponseDTO;
import com.supplychain.homologator.inventorysyncapi.repository.ProductRepository;
import com.supplychain.homologator.inventorysyncapi.repository.ProductSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final ProductRepository productRepository;
    private final FakeStoreClient fakeStoreClient;
    private final DummyJsonClient dummyJsonClient;

    
    @Scheduled(cron = "0 */10 * * * *")
    public void syncInventory() {
        log.info("Starting inventory sync at {}", LocalDateTime.now());
        
        CompletableFuture<Void> fakeStoreSync = CompletableFuture
        .runAsync(this::syncFromFakeStore)
        .exceptionally(ex -> {
            log.error("Error syncing FakeStore: {}", ex.getMessage());
            return null;
        });

CompletableFuture<Void> dummyJsonSync = CompletableFuture
        .runAsync(this::syncFromDummyJSON)
        .exceptionally(ex -> {
            log.error("Error syncing DummyJSON: {}", ex.getMessage());
            return null;
        });
        
        CompletableFuture.allOf(fakeStoreSync, dummyJsonSync).join();

        log.info("Inventory sync completed at {}", LocalDateTime.now());
    }
    
    private void syncFromFakeStore() {
        List<Product> products = fakeStoreClient.fetchProducts()
                .stream()
                .map(this::mapFakeStoreToProduct)
                .toList();

        if (!products.isEmpty()) {
            saveOrUpdateProducts(products);
            log.info("Saved {} products from FakeStore", products.size());
        }
    }
    
    private void syncFromDummyJSON() {
        List<Product> products = dummyJsonClient.fetchProducts()
                .stream()
                .map(this::mapDummyJsonToProduct)
                .toList();

        if (!products.isEmpty()) {
            saveOrUpdateProducts(products);
            log.info("Saved {} products from DummyJSON", products.size());
        }
    }
    
    private Product mapFakeStoreToProduct(FakeStoreProductDTO dto) {
        log.warn("Product FS_{} has no real stock, defaulting to 0", dto.id());

        return Product.builder()
                .internalId("FS_" + dto.id())
                .title(dto.title())
                .description(dto.description())
                .price(dto.price())
                .rating(dto.rating() != null ? dto.rating().rate() : 0.0)
                .stock(0)
                .auditStock(true)
                .category(dto.category())
                .provider("ProviderA")
                .lastSync(LocalDateTime.now())
                .build();
    }
    
    private Product mapDummyJsonToProduct(DummyJSONProductDTO dto) {
        return Product.builder()
                .internalId("DJ_" + dto.id())
                .title(dto.title())
                .description(dto.description())
                .price(dto.price())
                .rating(dto.rating())
                .stock(dto.stock() != null ? dto.stock() : 0)
                .auditStock(false)
                .category(dto.category())
                .provider("ProviderB")
                .lastSync(LocalDateTime.now())
                .build();
    }
   
    public List<ProductResponseDTO> getProducts(ProductFilter filter) {
        return productRepository
                .findAll(ProductSpecification.withFilters(filter))
                .stream()
                .map(ProductResponseDTO::from)
                .toList();
    }
    
   @Transactional
public int restockZeros(Integer newStock) {

    if (newStock == null || newStock <= 0) {
    log.error("Invalid newStock value: {}", newStock);
    throw new IllegalArgumentException("newStock must be greater than 0");
}

    int updated = productRepository.updateStockForZero(newStock);

    log.info("Updated {} products from stock 0 to {}",
            updated, newStock);

    return updated;
}
private void saveOrUpdateProducts(List<Product> products) {
    for (Product product : products) {
        productRepository.findByInternalId(product.getInternalId())
                .ifPresentOrElse(existing -> {
                    existing.setTitle(product.getTitle());
                    existing.setDescription(product.getDescription());
                    existing.setPrice(product.getPrice());
                    existing.setRating(product.getRating());
                    existing.setStock(product.getStock());
                    existing.setCategory(product.getCategory());
                    existing.setLastSync(product.getLastSync());
                    productRepository.save(existing);
                }, () -> productRepository.save(product));
    }
}
}