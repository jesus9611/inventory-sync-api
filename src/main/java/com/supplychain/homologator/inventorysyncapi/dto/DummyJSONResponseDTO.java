package com.supplychain.homologator.inventorysyncapi.dto;

import java.util.List;

public record DummyJSONResponseDTO(
    List<DummyJSONProductDTO> products,
    Integer total,
    Integer skip,
    Integer limit
) {}