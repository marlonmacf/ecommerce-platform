package com.mandrel.inventory_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mandrel.inventory_service.model.entity.Inventory;

public interface InventoryRepository extends JpaRepository<Inventory, String> {

    Inventory findByProductId(String productId);

    void deleteByProductId(String productId);
}