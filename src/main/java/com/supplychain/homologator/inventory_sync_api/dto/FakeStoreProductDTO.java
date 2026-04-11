package com.supplychain.homologator.inventory_sync_api.dto;

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