package com.supplychain.homologator.inventorysyncapi.dto;

public record DummyJSONProductDTO(
        Long id,
        String title,
        String description,
        Double price,
        Double rating,
        Integer stock,
        String category,
        String brand
) {}
