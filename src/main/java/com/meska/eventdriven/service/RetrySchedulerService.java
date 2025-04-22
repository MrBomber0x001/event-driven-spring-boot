package com.meska.eventdriven.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meska.eventdriven.dtos.OrderCreatedPayload;
import com.meska.eventdriven.dtos.UserRegisteredPayload;
import com.meska.eventdriven.entity.EventLog;
import com.meska.eventdriven.repos.EventLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;


@Service
@Slf4j
public class RetrySchedulerService {
    private final EventLogRepository eventLogRepository;
    private final EventPublisherService eventPublisherService;
    private final ObjectMapper objectMapper;

    public RetrySchedulerService(EventLogRepository eventLogRepository,
                               EventPublisherService eventPublisherService,
                               ObjectMapper objectMapper) {
        this.eventLogRepository = eventLogRepository;
        this.eventPublisherService = eventPublisherService;
        this.objectMapper = objectMapper;
    }
    @Scheduled(fixedDelayString = "${event.retry.interval}")
    @Transactional
    public void retryFailedEvents() {
        List<EventLog> retryEvents = eventLogRepository.findByStatusAndRetryCountLessThan(
                EventLog.EventStatus.RETRY, 3);

        log.info("Found {} events to retry", retryEvents.size());

        for (EventLog eventLog : retryEvents) {
            try {

                LocalDateTime lastUpdate = eventLog.getUpdatedAt();
                LocalDateTime now = LocalDateTime.now();
                int backoffSeconds = (1 << eventLog.getRetryCount()) * 5; // exponential backoff

                if (lastUpdate.plusSeconds(backoffSeconds).isAfter(now)) {
                    log.debug("Skipping event {} - not ready for retry yet", eventLog.getId());
                    continue; // skip this event if it's too soon to retry
                }

                eventPublisherService.retryEvent(eventLog);
            } catch (Exception e) {
                log.error("Failed to republish event {}: {}", eventLog.getId(), e.getMessage());
            }
        }
    }


}
