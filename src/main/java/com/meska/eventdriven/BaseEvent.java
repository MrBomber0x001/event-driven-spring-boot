package com.meska.eventdriven;

import com.meska.eventdriven.enums.EventsType;
import lombok.Getter;

@Getter
public class BaseEvent<T> {
    private final T payload;
    private final EventsType eventType;
    private final long timestamp;
    private final Long eventLogId;

    public BaseEvent(T payload, EventsType eventType, Long eventLogId) {
        this.payload = payload;
        this.eventType = eventType;
        this.timestamp = System.currentTimeMillis();
        this.eventLogId = eventLogId;
    }
}
