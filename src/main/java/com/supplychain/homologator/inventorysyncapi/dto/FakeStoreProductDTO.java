package com.supplychain.homologator.inventorysyncapi.dto;

public record FakeStoreProductDTO(
    Long id,
    String title,
    Double price,
    String description,
    String category,
    String image,
    RatingRecord rating
) {
    
    public record RatingRecord(Double rate, Integer count) {}
}