package com.mandrel.inventory_service.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mandrel.inventory_service.model.dto.InventoryDTO;
import com.mandrel.inventory_service.model.request.InventoryRequest;
import com.mandrel.inventory_service.service.InventoryService;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    @Autowired private InventoryService inventoryService;
    
    @GetMapping
    public List<InventoryDTO> getInventories() {
        return inventoryService.findAll();
    }

    @GetMapping("/{productId}")
    public ResponseEntity<InventoryDTO> getInventory(@PathVariable("productId") String productId) {
        InventoryDTO response = inventoryService.findById(productId);
        if (response != null) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<InventoryDTO> createInventory(@RequestBody InventoryRequest request) {
        return ResponseEntity.ok(inventoryService.createInventory(request.toInventory()));
    }

    @PutMapping("/{productId}")
    public ResponseEntity<InventoryDTO> atualizar(@PathVariable("productId") String productId, @RequestBody InventoryRequest request) {
        InventoryDTO response = inventoryService.updateInventoryById(productId, request.toInventory());
        if (response != null) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<?> remover(@PathVariable("productId") String productId) {
        InventoryDTO response = inventoryService.deleteById(productId);
        if (response != null) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.notFound().build();
    }
}
