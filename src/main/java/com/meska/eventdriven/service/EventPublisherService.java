package com.meska.eventdriven.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meska.eventdriven.BaseEvent;
import com.meska.eventdriven.dtos.OrderCreatedPayload;
import com.meska.eventdriven.dtos.UserRegisteredPayload;
import com.meska.eventdriven.entity.EventLog;
import com.meska.eventdriven.enums.EventsType;
import com.meska.eventdriven.repos.EventLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class EventPublisherService {
    private final ApplicationEventPublisher eventPublisher;
    private final EventLogRepository eventLogRepository;
    private final ObjectMapper objectMapper;

    public EventPublisherService(ApplicationEventPublisher eventPublisher, EventLogRepository eventLogRepository, ObjectMapper objectMapper) {
        this.eventPublisher = eventPublisher;
        this.eventLogRepository = eventLogRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public <T> void publishEvent(T payload, EventsType eventType) {
        try {
            String payloadJson = objectMapper.writeValueAsString(payload);

            //register new event in the log with status = pending
            EventLog eventLog = EventLog.builder()
                    .eventType(eventType)
                    .payload(payloadJson)
                    .status(EventLog.EventStatus.PENDING)
                    .retryCount(0)
                    .build();

            eventLog = eventLogRepository.save(eventLog);

            BaseEvent<T> event = new BaseEvent<>(payload, eventType, eventLog.getId());
            eventPublisher.publishEvent(event); // publish the event

            log.info("Published {} event with ID: {}", eventType, eventLog.getId());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize payload for event {}: {}", eventType, e.getMessage());
            throw new RuntimeException("Event publishing failed", e);
        }
    }

    @Transactional
    public <T> void retryEvent(EventLog eventLog) {
        try {
            // deserialize the payload from the existing event log
            T payload = (T) objectMapper.readValue(eventLog.getPayload(),
                    getPayloadTypeForEventType(eventLog.getEventType()));

            // update the event status back to PENDING before retry
            eventLog.setStatus(EventLog.EventStatus.PENDING);
            eventLogRepository.save(eventLog);

            // create a new BaseEvent but use the existing eventLog ID
            BaseEvent<T> event = new BaseEvent<>(payload, eventLog.getEventType(), eventLog.getId());
            eventPublisher.publishEvent(event);

            log.info("Retrying {} event with ID: {}", eventLog.getEventType(), eventLog.getId());
        } catch (Exception e) {
            log.error("Failed to retry event {}: {}", eventLog.getId(), e.getMessage());
            eventLog.setStatus(EventLog.EventStatus.FAILED); // after maximum count retryal count
            eventLog.setErrorMessage("Retry failed: " + e.getMessage());
            eventLogRepository.save(eventLog);
        }
    }

    // helper method to get the appropriate class type for deserialization
    private Class<?> getPayloadTypeForEventType(EventsType eventType) {
        switch (eventType) {
            case USER_REGISTERED:
                return UserRegisteredPayload.class;
            case ORDER_CREATED:
                return OrderCreatedPayload.class;
            default:
                throw new IllegalArgumentException("Unknown event type: " + eventType);
        }
    }
}