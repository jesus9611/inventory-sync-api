package com.supplychain.homologator.inventorysyncapi.dto;

public record ProductFilter(
        Double minRating,
        Double maxPrice,
        Integer minStock,
        String provider
) {}
