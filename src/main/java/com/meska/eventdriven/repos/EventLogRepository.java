package com.meska.eventdriven.repos;

import com.meska.eventdriven.entity.EventLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventLogRepository extends JpaRepository<EventLog, Long> {

    // finad all events with specific status and retry count less than max
    List<EventLog> findByStatusAndRetryCountLessThan(EventLog.EventStatus status, int maxRetries);

    // additional query methods as needed
    List<EventLog> findByStatus(EventLog.EventStatus status);

    // find failed events after a certain timestamp
    List<EventLog> findByStatusAndCreatedAtAfter(
            EventLog.EventStatus status,
            LocalDateTime timestamp
    );
}
