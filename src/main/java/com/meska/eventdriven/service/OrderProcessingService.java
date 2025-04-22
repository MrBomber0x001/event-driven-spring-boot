package com.meska.eventdriven.service;

import com.meska.eventdriven.dtos.OrderCreatedPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OrderProcessingService {
    public void processOrder(OrderCreatedPayload payload) {
        log.info("Order {} processed for product {} (${})",
                payload.getOrderId(), payload.getProductName(), payload.getAmount());
    }
}