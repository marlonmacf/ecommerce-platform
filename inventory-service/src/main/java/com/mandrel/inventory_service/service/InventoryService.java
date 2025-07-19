package com.mandrel.inventory_service.service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mandrel.inventory_service.model.dto.InventoryDTO;
import com.mandrel.inventory_service.model.entity.Inventory;
import com.mandrel.inventory_service.repository.InventoryRepository;

import jakarta.persistence.EntityManager;

@Service
public class InventoryService {

    @Autowired private EntityManager entityManager;
    @Autowired private InventoryRepository inventoryRepository;

    @Transactional
    public InventoryDTO createInventory(Inventory inventory) {
        inventory.setLastUpdate(Timestamp.from(Instant.now()));
        return InventoryDTO.Builder.build(entityManager.merge(inventory));
    }

    public List<InventoryDTO> findAll() {
        return InventoryDTO.Builder.build(inventoryRepository.findAll());
    }

    public InventoryDTO findById(String productId) {
        Inventory inventory = inventoryRepository.findByProductId(productId);
        if (inventory != null) {
            return InventoryDTO.Builder.build(inventory);
        }
        return null;
    }

    @Transactional
    public InventoryDTO deleteById(String productId) {
        Inventory inventory = inventoryRepository.findByProductId(productId);
        if (inventory != null) {
            inventoryRepository.deleteByProductId(productId);
            return InventoryDTO.Builder.build(inventory);
        }
        return null;
    }

    public InventoryDTO updateInventoryById(String productId, Inventory changes) {
        Inventory inventory = inventoryRepository.findByProductId(productId);
        if (inventory != null) {
            inventory.setStock(changes.getStock());
            inventory.setLastUpdate(Timestamp.from(Instant.now()));
            return InventoryDTO.Builder.build(inventoryRepository.save(inventory));
        }
        return null;
    }

    public Integer decrementStock(String productId, int quantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId);
        if (inventory != null) {
            Integer originalStock = inventory.getStock();
            if (quantity <= originalStock) {
                inventory.setStock(originalStock - quantity);
                inventoryRepository.save(inventory);
            }
            return originalStock;
        }
        return 0;
    }
}
