package com.supplychain.homologator.inventorysyncapi.client;

import com.supplychain.homologator.inventorysyncapi.dto.DummyJSONProductDTO;
import com.supplychain.homologator.inventorysyncapi.dto.DummyJSONResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DummyJsonClient {

    private final RestTemplate restTemplate;

    private static final String URL = "https://dummyjson.com/products?limit=100";

    public List<DummyJSONProductDTO> fetchProducts() {
        try {
            log.info("Fetching products from DummyJSON...");

            DummyJSONResponseDTO response = restTemplate.getForObject(
                    URL, DummyJSONResponseDTO.class);

            if (response == null || response.products() == null) {
                log.warn("DummyJSON returned null response");
                return Collections.emptyList();
            }

            log.info("DummyJSON returned {} products", response.products().size());
            return response.products();

        } catch (Exception e) {          
            log.error("DummyJSON is unavailable: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}