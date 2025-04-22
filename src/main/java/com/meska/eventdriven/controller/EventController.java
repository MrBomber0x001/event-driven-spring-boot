package com.meska.eventdriven.controller;

import com.meska.eventdriven.dtos.OrderCreatedPayload;
import com.meska.eventdriven.dtos.UserRegisteredPayload;
import com.meska.eventdriven.enums.EventsType;
import com.meska.eventdriven.service.EventPublisherService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {
    private final EventPublisherService eventPublisherService;

    @PostMapping("/register-user")
    public String registerUser(@RequestBody UserRegisteredPayload payload) {
        eventPublisherService.publishEvent(payload, EventsType.USER_REGISTERED);
        return "User registration event published";
    }

    @PostMapping("/create-order")
    public String createOrder(@RequestBody OrderCreatedPayload payload) {
        eventPublisherService.publishEvent(payload, EventsType.ORDER_CREATED);
        return "Order created event published";
    }
}
