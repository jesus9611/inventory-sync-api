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
                .runAsync(this::syncFromFakeStore);

        CompletableFuture<Void> dummyJsonSync = CompletableFuture
                .runAsync(this::syncFromDummyJSON);
        
        CompletableFuture.allOf(fakeStoreSync, dummyJsonSync).join();

        log.info("Inventory sync completed at {}", LocalDateTime.now());
    }
    
    private void syncFromFakeStore() {
        List<Product> products = fakeStoreClient.fetchProducts()
                .stream()
                .map(this::mapFakeStoreToProduct)
                .toList();

        if (!products.isEmpty()) {
            productRepository.saveAll(products);
            log.info("Saved {} products from FakeStore", products.size());
        }
    }
    
    private void syncFromDummyJSON() {
        List<Product> products = dummyJsonClient.fetchProducts()
                .stream()
                .map(this::mapDummyJsonToProduct)
                .toList();

        if (!products.isEmpty()) {
            productRepository.saveAll(products);
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
    
    public int restockZeros(Integer newStock) {
        List<Product> zeroStockProducts = productRepository
                .findAll()
                .stream()
                .filter(p -> p.getStock() == 0)
                .toList();

        if (zeroStockProducts.isEmpty()) {
            log.info("No products with zero stock found");
            return 0;
        }

        zeroStockProducts.forEach(p -> p.setStock(newStock));
        productRepository.saveAll(zeroStockProducts);

        log.info("Updated {} products from stock 0 to {}",
                zeroStockProducts.size(), newStock);
        return zeroStockProducts.size();
    }
}