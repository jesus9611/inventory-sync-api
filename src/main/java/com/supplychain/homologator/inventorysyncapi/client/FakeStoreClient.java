package com.supplychain.homologator.inventorysyncapi.client;

import com.supplychain.homologator.inventorysyncapi.dto.FakeStoreProductDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FakeStoreClient {

    private final RestTemplate restTemplate;

    private static final String URL = "https://fakestoreapi.com/products";

    public List<FakeStoreProductDTO> fetchProducts() {
        try {
            log.info("Fetching products from FakeStore...");

            FakeStoreProductDTO[] response = restTemplate.getForObject(
                    URL, FakeStoreProductDTO[].class);

            if (response == null) {
                log.warn("FakeStore returned null response");
                return Collections.emptyList();
            }

            log.info("FakeStore returned {} products", response.length);
            return Arrays.asList(response);

        } catch (Exception e) {            
            log.error("FakeStore is unavailable: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}