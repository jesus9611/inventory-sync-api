package com.supplychain.homologator.inventorysyncapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.supplychain.homologator.inventorysyncapi.domain.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
    
}