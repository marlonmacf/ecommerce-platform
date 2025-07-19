package com.mandrel.processing_service.model.request;

public class OrderRequest {
    private Long id;

    public OrderRequest(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
