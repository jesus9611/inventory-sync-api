package com.supplychain.homologator.inventorysyncapi.repository;

import com.supplychain.homologator.inventorysyncapi.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List; 

public interface ProductRepository extends JpaRepository<Product, String>, JpaSpecificationExecutor<Product> {

    List<Product> findByStock(Integer stock);
}