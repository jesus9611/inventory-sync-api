package com.supplychain.homologator.inventorysyncapi.service;

import com.supplychain.homologator.inventorysyncapi.domain.Product;
import com.supplychain.homologator.inventorysyncapi.dto.DummyJSONProductDTO;
import com.supplychain.homologator.inventorysyncapi.dto.DummyJSONResponseDTO;
import com.supplychain.homologator.inventorysyncapi.dto.FakeStoreProductDTO;
import com.supplychain.homologator.inventorysyncapi.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final ProductRepository productRepository;
    private final RestTemplate restTemplate;

    
    private final String FAKESTORE_URL = "https://fakestoreapi.com/products";
    private final String DUMMYJSON_URL = "https://dummyjson.com/products";

    
    private Product mapToProduct(FakeStoreProductDTO fs) {
    return Product.builder()
            .internalId("FS_" + fs.id())
            .title(fs.title())
            .description(fs.description())
            .price(fs.price())        
            .rating(0.0) 
            .stock(10)
            .category(fs.category())
            .provider("FakeStore")
            .lastSync(LocalDateTime.now())
            .auditStock(true)
            .build();
}


private Product mapToProduct(DummyJSONProductDTO dj) {
    return Product.builder()
            .internalId("DJ_" + dj.id())
            .title(dj.title())
            .description(dj.description())
            .price(dj.price())
            .rating(dj.rating())
            .stock(dj.stock())
            .category(dj.category())
            .provider("DummyJSON")
            .lastSync(LocalDateTime.now())
            .auditStock(dj.stock() > 0)
            .build();
}
   public void syncInventory() {
    try {
        FakeStoreProductDTO[] fsResponse = restTemplate.getForObject(FAKESTORE_URL, FakeStoreProductDTO[].class);
        if (fsResponse != null) {
            List<Product> products = Arrays.stream(fsResponse)
                    .map(this::mapToProduct)
                    .collect(Collectors.toList());
            productRepository.saveAll(products);
        }
    } catch (Exception e) {
        System.out.println("Error al conectar con FakeStore: " + e.getMessage());
    }

    
    try {
        DummyJSONResponseDTO djResponse = restTemplate.getForObject(DUMMYJSON_URL, DummyJSONResponseDTO.class);
        if (djResponse != null && djResponse.products() != null) {
            List<Product> products = djResponse.products().stream()
                    .map(this::mapToProduct)
                    .collect(Collectors.toList());
            productRepository.saveAll(products);
        }
    } catch (Exception e) {
        System.out.println("Error al conectar con DummyJSON: " + e.getMessage());
    }
}
}