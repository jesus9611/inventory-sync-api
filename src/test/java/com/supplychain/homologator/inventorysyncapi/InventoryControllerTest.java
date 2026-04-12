package com.supplychain.homologator.inventorysyncapi;

import com.supplychain.homologator.inventorysyncapi.controller.InventoryController;
import com.supplychain.homologator.inventorysyncapi.service.InventoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import com.supplychain.homologator.inventorysyncapi.dto.RestockRequest;

@ExtendWith(MockitoExtension.class)
class InventoryControllerTest {

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private InventoryController inventoryController;

    @Test
    void shouldRestockZerosSuccessfully() {
        
        when(inventoryService.restockZeros(10)).thenReturn(5);

    
        ResponseEntity<?> response = inventoryController.restockZeros(
    new RestockRequest(10));

        
        assertThat(response.getStatusCode().value()).isEqualTo(200);
    }
}