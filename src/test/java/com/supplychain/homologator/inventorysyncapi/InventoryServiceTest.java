package com.supplychain.homologator.inventorysyncapi;

import com.supplychain.homologator.inventorysyncapi.client.DummyJsonClient;
import com.supplychain.homologator.inventorysyncapi.client.FakeStoreClient;
import com.supplychain.homologator.inventorysyncapi.dto.FakeStoreProductDTO;
import com.supplychain.homologator.inventorysyncapi.repository.ProductRepository;
import com.supplychain.homologator.inventorysyncapi.service.InventoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private FakeStoreClient fakeStoreClient;

    @Mock
    private DummyJsonClient dummyJsonClient;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    void shouldPersistProductsWhenProductsExist() {
        List<FakeStoreProductDTO> products = List.of(
                new FakeStoreProductDTO(
                        1L, "Product 1", "desc", 10.0, "cat", "image-url",
                        new FakeStoreProductDTO.RatingRecord(4.5, 10)
                ),
                new FakeStoreProductDTO(
                        2L, "Product 2", "desc", 20.0, "cat", "image-url",
                        new FakeStoreProductDTO.RatingRecord(4.0, 8)
                )
        );

        when(fakeStoreClient.fetchProducts()).thenReturn(products);
        when(dummyJsonClient.fetchProducts()).thenReturn(Collections.emptyList());

        inventoryService.syncInventory();

        verify(productRepository).saveAll(anyList());
    }

    @Test
    void shouldNotPersistWhenNoProducts() {
        when(fakeStoreClient.fetchProducts()).thenReturn(Collections.emptyList());
        when(dummyJsonClient.fetchProducts()).thenReturn(Collections.emptyList());

        inventoryService.syncInventory();

        verify(productRepository, never()).saveAll(anyList());
    }

    @Test
    void shouldPersistProductsFromBothProviders() {
        List<FakeStoreProductDTO> fakeProducts = List.of(
                new FakeStoreProductDTO(
                        1L, "Product 1", "desc", 10.0, "cat", "img",
                        new FakeStoreProductDTO.RatingRecord(4.5, 10)
                )
        );

        when(fakeStoreClient.fetchProducts()).thenReturn(fakeProducts);
        when(dummyJsonClient.fetchProducts()).thenReturn(Collections.emptyList());

        inventoryService.syncInventory();

        verify(productRepository, atLeastOnce()).saveAll(anyList());
    }
}