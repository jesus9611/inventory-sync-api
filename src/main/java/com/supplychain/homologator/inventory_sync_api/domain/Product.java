package com.supplychain.homologator.inventory_sync_api.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    private String internalId;
    private String title;
    private String description;
    private Double price;
    private Double rating;
    private Integer stock;
    private String category;
    private String provider;
    
    
    private LocalDateTime lastSync;
    private Boolean auditStock; 
}