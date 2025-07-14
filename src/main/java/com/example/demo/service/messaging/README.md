# RabbitMQ Module Documentation

## Overview
This module provides a complete RabbitMQ integration for the Class entity with clean code and SOLID principles.

## Components

### 1. Configuration
- **RabbitMQConfig.java**: Main configuration with direct exchange, queues, and bindings
- **RabbitMQProperties.java**: Type-safe configuration properties

### 2. DTOs
- **ClassMessage.java**: Message DTO with factory methods for different scenarios

### 3. Services
- **ClassMessagingService.java**: Interface defining messaging operations (ISP principle)
- **ClassMessagePublisher.java**: Implementation for publishing messages
- **ClassMessageConsumer.java**: Message listener/consumer

### 4. Properties Configuration
```properties
# RabbitMQ Connection
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=admin
spring.rabbitmq.password=password

# Custom Queues and Exchanges
app.rabbitmq.class.queue.name=class.queue
app.rabbitmq.class.exchange.name=class.exchange
app.rabbitmq.class.routing-key=class.routing.key
app.rabbitmq.class.dead-letter.queue.name=class.dlq
app.rabbitmq.class.dead-letter.exchange.name=class.dlx
```

## Usage in ClassService

### Inject the messaging service:
```java
@Service
@RequiredArgsConstructor
public class ClassServiceImpl implements ClassService {
    
    private final ClassRepository classRepository;
    private final ClassMessagingService messagingService; // Inject this
    
    @Override
    public ClassResponse createClass(ClassRequest request) {
        try {
            // Your existing logic
            ClassEntity savedEntity = classRepository.save(entity);
            ClassResponse response = mapper.toResponse(savedEntity);
            
            // Send notification
            messagingService.notifyClassCreated(response.getId(), response.getName());
            
            return response;
        } catch (Exception e) {
            messagingService.notifyClassOperationFailed(null, request.getName(), "CREATE", e.getMessage());
            throw e;
        }
    }
}
```

## Features

### ✅ SOLID Principles Applied:
- **SRP**: Each class has single responsibility
- **OCP**: Extensible through interfaces
- **LSP**: Proper inheritance hierarchy
- **ISP**: Interface segregation with focused contracts
- **DIP**: Depends on abstractions, not concretions

### ✅ Enterprise Features:
- Dead Letter Queue (DLQ) for failed messages
- Message TTL (5 minutes)
- Connection retry logic
- JSON message serialization
- Confirm and return callbacks
- Concurrent consumers (3-10)

### ✅ Message Types:
- Class creation notifications
- Class update notifications
- Class deletion notifications
- Error/failure notifications

## Testing
Start RabbitMQ with Docker:
```bash
docker-compose up -d rabbitmq
```

Access Management UI: http://localhost:15672
- Username: admin
- Password: password

## Queue Structure
```
class.exchange (Direct) -> class.queue -> Your Application
                      |
                      -> class.dlx -> class.dlq (Failed messages)
```

This module is ready to use in your ClassService!
