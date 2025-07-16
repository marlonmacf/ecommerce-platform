package com.processing_service.processing_service.model.request;

public class InventoryRequest {
    private String productId;
    private Integer stock;

    public InventoryRequest(String productId, Integer stock) {
        this.productId = productId;
        this.stock = stock;
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
}