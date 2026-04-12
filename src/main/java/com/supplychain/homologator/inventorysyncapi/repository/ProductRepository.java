package com.supplychain.homologator.inventorysyncapi.repository;

import com.supplychain.homologator.inventorysyncapi.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, String>, JpaSpecificationExecutor<Product> {

    List<Product> findByStock(Integer stock);

    Optional<Product> findByInternalId(String internalId);

    @Modifying
    @Query("UPDATE Product p SET p.stock = :newStock WHERE p.stock = 0")
    int updateStockForZero(@Param("newStock") Integer newStock);
}