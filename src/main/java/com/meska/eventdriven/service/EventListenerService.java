package com.meska.eventdriven.service;

import com.meska.eventdriven.BaseEvent;
import com.meska.eventdriven.dtos.OrderCreatedPayload;
import com.meska.eventdriven.dtos.UserRegisteredPayload;
import com.meska.eventdriven.entity.EventLog;
import com.meska.eventdriven.repos.EventLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventListenerService {
    private final EventLogRepository eventLogRepository;
    private final NotificationService notificationService;
    private final OrderProcessingService orderProcessingService;

    @Value("${event.max.retries}")
    private int maxRetries;

    @Async
    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleEvent(BaseEvent<?> event) {
        EventLog eventLog = eventLogRepository.findById(event.getEventLogId())
                .orElseThrow(() -> new RuntimeException("Event log not found"));

        if (eventLog.getStatus() == EventLog.EventStatus.RETRY ||
                eventLog.getStatus() == EventLog.EventStatus.FAILED ||
                eventLog.getStatus() == EventLog.EventStatus.PROCESSED) {
            log.info("Skipping event {} as it's already in status: {}",
                    event.getEventLogId(), eventLog.getStatus());
            return;
        }
        try {
            switch (event.getEventType()) {
                case USER_REGISTERED:
                    handleUserRegistration((BaseEvent<UserRegisteredPayload>) event);
                    break;
                case ORDER_CREATED:
                    handleOrderCreated((BaseEvent<OrderCreatedPayload>) event);
                    break;
                default:
                    log.warn("Unhandled event type: {}", event.getEventType());
            }

            // mark as processed
            eventLog.setStatus(EventLog.EventStatus.PROCESSED);
            eventLog.setProcessedAt(LocalDateTime.now());
            eventLogRepository.save(eventLog);

        } catch (Exception e) {
            handleEventFailure(eventLog, e);
        }
        }

        public void handleEventFailure(EventLog eventLog, Exception e){
        log.info("got here inside handleEventHandler: {}", eventLog);
            int newRetryCount = eventLog.getRetryCount() + 1;
            eventLog.setStatus(newRetryCount >= maxRetries ?
                    EventLog.EventStatus.FAILED : EventLog.EventStatus.RETRY);
            eventLog.setRetryCount(newRetryCount);
            eventLog.setErrorMessage(e.getMessage());
            eventLogRepository.save(eventLog);

            log.error("Event processing failed (retry {}/{}): {}",
                    newRetryCount, maxRetries, e.getMessage());
        }

    private void handleUserRegistration(BaseEvent<UserRegisteredPayload> event) {
        UserRegisteredPayload payload = event.getPayload();
        log.info("Processing user registration: {}", payload.getEmail());

        // Simulate processing
        if ("fail@example.com".equals(payload.getEmail())) {
            throw new RuntimeException("Simulated failure for email: " + payload.getEmail());
        }

        notificationService.sendWelcomeEmail(payload.getEmail());
    }

    private void handleOrderCreated(BaseEvent<OrderCreatedPayload> event) {
        OrderCreatedPayload payload = event.getPayload();
        log.info("Processing order: {}", payload.getOrderId());

        // Simulate processing
        if ("FAIL123".equals(payload.getOrderId())) {
            throw new RuntimeException("Simulated failure for order: " + payload.getOrderId());
        }

        orderProcessingService.processOrder(payload);
    }
}
