package com.mandrel.processing_service.model.dto;

import java.sql.Timestamp;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {
    
    private Long id;
    private String status;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSSSSSSSS")
    private Timestamp orderDate;
    private List<OrderItemEvent> items;

    @Override
    public String toString() {
        return "{id:" + id + ", status:\"" + status + "\", orderDate:\"" + orderDate + "\", items:" + items + "}";
    }
}