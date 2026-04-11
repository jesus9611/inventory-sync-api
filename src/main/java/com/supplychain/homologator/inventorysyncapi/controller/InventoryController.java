package com.supplychain.homologator.inventorysyncapi.controller;

import com.supplychain.homologator.inventorysyncapi.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping("/sync")
    public String sync() {
        inventoryService.syncInventory();
        return "Sincronización completada con éxito";
    }
}