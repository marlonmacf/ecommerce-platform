package com.inventory_service.inventory_service.model.request;

import com.inventory_service.inventory_service.model.entity.Inventory;

public class InventoryRequest {

    private String productId;
    private Integer stock;

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Inventory toInventory() {
        Inventory inventory = new Inventory();
        inventory.setProductId(this.productId);
        inventory.setStock(this.stock);   
        return inventory;
    }
}
