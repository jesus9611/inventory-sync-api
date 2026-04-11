package com.supplychain.homologator.inventorysyncapi;

import com.supplychain.homologator.inventorysyncapi.client.DummyJsonClient;
import com.supplychain.homologator.inventorysyncapi.client.FakeStoreClient;
import com.supplychain.homologator.inventorysyncapi.repository.ProductRepository;
import com.supplychain.homologator.inventorysyncapi.service.InventoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
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
    void shouldNotPersistWhenNoProducts() {
        when(fakeStoreClient.fetchProducts())
            .thenReturn(Collections.emptyList());

        inventoryService.syncInventory();

        verify(productRepository, never()).saveAll(anyList());
    }

    
}