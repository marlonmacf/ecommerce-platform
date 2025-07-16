package com.inventory_service.inventory_service.model.dto;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.inventory_service.inventory_service.model.entity.Inventory;

public class InventoryDTO {

    private String productId;
    private Integer stock;
    private Timestamp lastUpdate;

    private InventoryDTO(Builder builder) {
        this.productId = builder.productId;
        this.stock = builder.stock;
        this.lastUpdate = builder.lastUpdate;
    }

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

    public Timestamp getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Timestamp lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public static class Builder {
        private String productId;
        private Integer stock;
        private Timestamp lastUpdate;

        public Builder(String productId, Integer stock, Timestamp lastUpdate) {
            this.productId = productId;
            this.stock = stock;
            this.lastUpdate = lastUpdate;
        }

        public static List<InventoryDTO> build(List<Inventory> inventories) {
            List<InventoryDTO> response = new ArrayList<>();
            for (Inventory inventory: inventories) {
                response.add(build(inventory));
            }
            return response;
        }

        public static InventoryDTO build(Inventory inventory) {
            if (inventory != null) {
                return new InventoryDTO(new Builder(inventory.getProductId(), inventory.getStock(), inventory.getLastUpdate()));
            }
            return null;   
        }

        public InventoryDTO build() {
            return new InventoryDTO(this);
        }
    }
}
