# Event-Driven Architecture with Spring Boot

This project demonstrates an implementation of event-driven architecture in a Spring Boot application, featuring robust event publishing, processing, and retry mechanisms.

## Overview

The system provides a reliable way to process events asynchronously with built-in failure handling and automatic retries. Events are persisted in a database before processing, ensuring that no events are lost even if the application crashes.

## Features

- **Event Persistence**: All events are stored in a database before processing
- **Asynchronous Processing**: Events are processed asynchronously using Spring's event system
- **Automatic Retry**: Failed events are automatically retried with exponential backoff
- **Transaction Safety**: Event processing is wrapped in transactions to ensure data consistency
- **Status Tracking**: Full tracking of event processing status (PENDING, PROCESSED, RETRY, FAILED)

## Architecture

The system consists of several key components:

1. **EventPublisherService**: Publishes events to the Spring event system while persisting them to the database
2. **EventListenerService**: Listens for and processes events asynchronously
3. **RetrySchedulerService**: Schedules retries for failed events with exponential backoff
4. **EventLogRepository**: Persists event logs and their processing status

## Event Processing Flow

1. A service publishes an event using `EventPublisherService`
2. The event is stored in the database with status `PENDING`
3. The event is published to the Spring event system
4. `EventListenerService` processes the event asynchronously
5. If processing succeeds, the event status is updated to `PROCESSED`
6. If processing fails, the event status is updated to `RETRY` and retry count incremented
7. `RetrySchedulerService` periodically checks for events with status `RETRY`
8. Failed events are retried with exponential backoff until they succeed or exceed the maximum retry count
9. If maximum retries are exceeded, the event status is updated to `FAILED`

## Getting Started

### Prerequisites

- Java 17+
- Maven or Gradle

### Configuration

Key configuration properties (in `application.properties`):

```properties
# Database configuration
spring.datasource.url=jdbc:h2:file:./data/eventdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.h2.console.enabled=true

# Event configuration
event.max.retries=3
event.retry.interval=5000
```

### Usage Example

Publishing an event:

```java
@Service
@RequiredArgsConstructor
public class UserService {
    private final EventPublisherService eventPublisherService;
    
    public void registerUser(User user) {
        // Save user to database
        userRepository.save(user);
        
        // Publish event
        UserRegisteredPayload payload = new UserRegisteredPayload(user.getId(), user.getEmail());
        eventPublisherService.publishEvent(payload, EventsType.USER_REGISTERED);
    }
}
```

Processing an event:

```java
private void handleUserRegistration(BaseEvent<UserRegisteredPayload> event) {
    UserRegisteredPayload payload = event.getPayload();
    log.info("Processing user registration: {}", payload.getEmail());
    
    // Business logic here
    notificationService.sendWelcomeEmail(payload.getEmail());
}
```

## Best Practices

1. **Always make event handlers idempotent**: An event might be processed multiple times in case of retries.
2. **Keep event payloads small**: Include only necessary identifiers, not entire objects.
3. **Handle all exceptions**: Catch and properly handle all exceptions in event handlers.
4. **Use transaction boundaries wisely**: Separate the event processing transaction from the publishing transaction.
5. **Monitor event processing**: Keep track of failed events and processing times.
