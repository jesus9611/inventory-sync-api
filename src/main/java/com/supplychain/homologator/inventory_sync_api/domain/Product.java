package com.supplychain.homologator.inventory_sync_api.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private Double price;
    private Double rating; 
    private Integer stock;
    private String provider;
}
