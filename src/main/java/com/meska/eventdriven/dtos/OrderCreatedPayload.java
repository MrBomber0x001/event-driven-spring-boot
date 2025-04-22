package com.meska.eventdriven.dtos;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedPayload {
    private String orderId;
    private String productName;
    private double amount;
}
