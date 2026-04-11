package com.supplychain.homologator.inventorysyncapi.repository;

import com.supplychain.homologator.inventorysyncapi.domain.Product;
import com.supplychain.homologator.inventorysyncapi.dto.ProductFilter;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {

    
    private ProductSpecification() {}

    public static Specification<Product> withFilters(ProductFilter filter) {
        return (root, query, criteriaBuilder) -> {
            
            List<Predicate> predicates = new ArrayList<>();
            
            if (filter.minRating() != null) {
                predicates.add(
                    criteriaBuilder.greaterThanOrEqualTo(
                        root.get("rating"), filter.minRating()
                    )
                );
               
            }

            if (filter.maxPrice() != null) {
                predicates.add(
                    criteriaBuilder.lessThanOrEqualTo(
                        root.get("price"), filter.maxPrice()
                    )
                );
               
            }

            if (filter.minStock() != null) {
                predicates.add(
                    criteriaBuilder.greaterThanOrEqualTo(
                        root.get("stock"), filter.minStock()
                    )
                );
                
            }

            if (filter.provider() != null && !filter.provider().isBlank()) {
                predicates.add(
                    criteriaBuilder.equal(
                        root.get("provider"), filter.provider()
                    )
                );
               
            }

           
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}