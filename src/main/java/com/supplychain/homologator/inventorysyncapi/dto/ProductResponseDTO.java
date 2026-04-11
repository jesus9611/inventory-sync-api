package com.supplychain.homologator.inventorysyncapi.dto;

import com.supplychain.homologator.inventorysyncapi.domain.Product;
import java.time.LocalDateTime;

public record ProductResponseDTO(
        String internalId,
        String title,
        String description,
        Double price,
        Double rating,
        Integer stock,
        String category,
        String provider,
        LocalDateTime lastSync,
        Boolean auditStock
) {
   
    public static ProductResponseDTO from(Product product) {
        return new ProductResponseDTO(
                product.getInternalId(),
                product.getTitle(),
                product.getDescription(),
                product.getPrice(),
                product.getRating(),
                product.getStock(),
                product.getCategory(),
                product.getProvider(),
                product.getLastSync(),
                product.getAuditStock()
        );
    }
}