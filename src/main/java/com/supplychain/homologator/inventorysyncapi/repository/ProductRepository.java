package com.supplychain.homologator.inventorysyncapi.repository;

import com.supplychain.homologator.inventorysyncapi.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, String>,
        JpaSpecificationExecutor<Product> {
}