package com.meska.eventdriven.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationService {
    public void sendWelcomeEmail(String email) {
        log.info("Sending welcome email to: {}", email);
    }
}