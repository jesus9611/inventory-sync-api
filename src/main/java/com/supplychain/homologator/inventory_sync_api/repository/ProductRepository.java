package com.supplychain.homologator.inventory_sync_api.repository;

import com.supplychain.homologator.inventory_sync_api.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
    
}