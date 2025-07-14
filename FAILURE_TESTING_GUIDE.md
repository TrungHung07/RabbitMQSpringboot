# RabbitMQ Failure Testing Guide

## Overview
This guide helps you systematically test RabbitMQ messaging behavior when database operations fail AND when consumer processing fails (DLQ scenarios).

## Test Categories

### A. Application-Level Failure Tests (Service Layer)
These test business logic failures that are handled gracefully:

### 1. Duplicate Key Constraint Test
**Endpoint:** `POST /api/v1/test/duplicate-key-error`
**Purpose:** Triggers the duplicate key constraint violation you saw in the logs
**Expected Behavior:**
- Database operation fails
- RabbitMQ message is sent with `status=FAILED`
- Consumer processes the failure message
- Error is logged properly

**Test Command:**
```bash
curl -X POST http://localhost:8080/api/v1/test/duplicate-key-error
```

### 2. Null Name Validation Test
**Endpoint:** `POST /api/v1/test/null-name-error`
**Purpose:** Tests what happens when validation fails
**Expected Behavior:**
- Validation error occurs
- Failure message sent to RabbitMQ
- Consumer handles validation failure

**Test Command:**
```bash
curl -X POST http://localhost:8080/api/v1/test/null-name-error
```

### 3. Non-Existent Class Fetch Test
**Endpoint:** `GET /api/v1/test/nonexistent-class/{id}`
**Purpose:** Tests fetching a class that doesn't exist
**Expected Behavior:**
- "Class not found" exception
- No RabbitMQ message (since it's a read operation)

**Test Command:**
```bash
curl -X GET http://localhost:8080/api/v1/test/nonexistent-class/99999
```

### 4. Update Non-Existent Class Test
**Endpoint:** `PUT /api/v1/test/update-nonexistent/{id}`
**Purpose:** Tests updating a class that doesn't exist
**Expected Behavior:**
- "Class not found" exception
- RabbitMQ failure message sent
- Consumer processes update failure

**Test Command:**
```bash
curl -X PUT http://localhost:8080/api/v1/test/update-nonexistent/99999
```

### 5. Delete Non-Existent Class Test
**Endpoint:** `DELETE /api/v1/test/delete-nonexistent/{id}`
**Purpose:** Tests deleting a class that doesn't exist
**Expected Behavior:**
- "Class not found" exception
- RabbitMQ failure message sent
- Consumer processes deletion failure

**Test Command:**
```bash
curl -X DELETE http://localhost:8080/api/v1/test/delete-nonexistent/99999
```

### 6. Manual ID Creation Test
**Endpoint:** `POST /api/v1/test/create-with-manual-id?manualId=0`
**Purpose:** Intentionally creates classes with ID=0 to trigger duplicates
**Expected Behavior:**
- Duplicate key constraint violation
- Multiple failure messages
- Queue fills with error messages

**Test Command:**
```bash
curl -X POST "http://localhost:8080/api/v1/test/create-with-manual-id?manualId=0"
```

---

### B. DLQ (Dead Letter Queue) Tests (Consumer Failures)
These test message processing failures that trigger DLQ:

### 7. Poison Message Test
**Endpoint:** `POST /api/v1/test/dlq/send-poison-message`
**Purpose:** Sends a message designed to fail in the consumer
**Expected Behavior:**
- Consumer receives message
- Consumer throws exception due to invalid data
- Message is requeued and retried
- After max retries, message goes to DLQ

**Test Command:**
```bash
curl -X POST http://localhost:8080/api/v1/test/dlq/send-poison-message
```

### 8. Malformed Message Test
**Endpoint:** `POST /api/v1/test/dlq/send-malformed-message`
**Purpose:** Sends a message with wrong JSON structure
**Expected Behavior:**
- Message deserialization fails
- Consumer cannot process message
- Message goes to DLQ due to parsing error

**Test Command:**
```bash
curl -X POST http://localhost:8080/api/v1/test/dlq/send-malformed-message
```

### 9. Runtime Exception Test
**Endpoint:** `POST /api/v1/test/dlq/send-exception-trigger`
**Purpose:** Triggers a runtime exception in the consumer
**Expected Behavior:**
- Consumer receives message successfully
- Consumer throws RuntimeException during processing
- Message is retried and eventually sent to DLQ

**Test Command:**
```bash
curl -X POST http://localhost:8080/api/v1/test/dlq/send-exception-trigger
```

### 10. Batch DLQ Test
**Endpoint:** `POST /api/v1/test/dlq/batch-failure-test?messageCount=5`
**Purpose:** Sends multiple messages that will all fail
**Expected Behavior:**
- Multiple messages fail processing
- All messages end up in DLQ
- Tests DLQ handling under load

**Test Command:**
```bash
curl -X POST "http://localhost:8080/api/v1/test/dlq/batch-failure-test?messageCount=10"
```

## What to Watch For in Logs

### 1. Service Layer Logs (Application Failures)
Look for these patterns in `ClassServiceImpl`:
```
ERROR - Error creating class with name: {}: {}
INFO - Class creation notification sent to RabbitMQ for id: {}
```

### 2. Consumer Layer Logs (DLQ Triggers)
Look for these patterns in `ClassMessageConsumer`:
```
ERROR - DLQ TEST: Processing poison message with invalid ID: {}
ERROR - DLQ TEST: Throwing runtime exception as requested
ERROR - Failed to process class message: {}, Error: {}
```

### 3. DLQ Handler Logs
Look for messages being processed by DLQ handler:
```
ERROR - Received message from Dead Letter Queue: {}
ERROR - Message processing failed multiple times for class ID: {}
```

### 4. Messaging Publisher Logs
Look for these patterns in `ClassMessagePublisher`:
```
INFO - Publishing class message: ClassMessage(...)
INFO - Successfully published class message for class ID: {}
```

### 5. Messaging Consumer Logs
Look for these patterns in `ClassMessageConsumer`:
```
INFO - Received class message: ClassMessage(...)
INFO - Processing class creation for ID: {}, Name: {}
ERROR - Failed to process class message: {}
```

### 6. Database Error Patterns
Watch for PostgreSQL constraint violations:
```
ERROR: duplicate key value violates unique constraint "class_pkey"
Detail: Key (id)=(0) already exists.
```

## Testing Sequence

1. **Start Services:**
   ```bash
   docker-compose up -d
   ```

2. **Start Spring Boot App:**
   ```bash
   mvn spring-boot:run
   ```

3. **Run Tests in Order:**
   - **Phase 1: Application Failures** (Tests 1-6)
     - These test your service layer error handling
     - Messages will be sent successfully to RabbitMQ with status=FAILED
     - No DLQ activity expected
   
   - **Phase 2: DLQ Testing** (Tests 7-10)  
     - These test consumer failures
     - Messages will fail processing and go to DLQ
     - Check DLQ queue for messages
   
   - **Phase 3: Verification**
     - Check RabbitMQ Management UI for message statistics
     - Verify DLQ has received failed messages

## RabbitMQ Management UI

Access: http://localhost:15672
- Username: guest
- Password: guest

**What to Check:**
- **Main Queue:** `class.queue` - see message processing rate
- **Dead Letter Queue:** `class.queue.dlq` - see failed messages accumulating  
- **Exchanges:** `class.exchange` - see message routing statistics
- **Message rates:** Compare successful vs failed processing
- **DLQ Message Details:** Click on DLQ to see actual failed messages

## Expected Message Structures

### Application Failure Messages (Processed Successfully)
```json
{
  "classId": null,
  "className": "Test Class Name",
  "action": "CREATE",
  "status": "FAILED",
  "message": "could not execute statement [ERROR: duplicate key...]",
  "timestamp": "2025-07-14T09:50:09",
  "payload": null
}
```

### DLQ Messages (Consumer Processing Failures)
```json
{
  "classId": -999,
  "className": "POISON_MESSAGE_TEST", 
  "action": "CREATE",
  "status": "SUCCESS",
  "message": "This message is designed to fail in consumer",
  "timestamp": "2025-07-14T09:50:09",
  "payload": "TRIGGER_CONSUMER_FAILURE"
}
```

## Troubleshooting

### If you don't see application failure messages:
1. Check if RabbitMQ is running: `docker ps`
2. Check Spring Boot logs for connection errors
3. Verify `ClassMessagingService` is being called in catch blocks
4. Check RabbitMQ Management UI for queue activity

### If you don't see DLQ messages:
1. Verify DLQ configuration in `RabbitMQConfig.java`
2. Check consumer is throwing exceptions (not catching them)
3. Verify DLQ queue exists: `class.queue.dlq`
4. Check retry settings - messages need to fail multiple times
5. Look for consumer exception logs

### Common DLQ Issues:
- **Messages not reaching DLQ:** Consumer is catching exceptions instead of throwing them
- **DLQ queue not created:** Check RabbitMQ configuration
- **Messages processed successfully:** Test triggers not working in consumer logic

## Learning Objectives

Through this testing, you should observe:

### Application-Level Failures (Tests 1-6):
1. How exceptions trigger RabbitMQ failure notifications
2. How consumers handle failed operation messages normally
3. How the system maintains consistency between database and messaging
4. How error messages flow through the entire system
5. The difference between business logic failures and infrastructure failures

### DLQ-Level Failures (Tests 7-10):
1. **Consumer Exception Handling:** How unhandled exceptions trigger message requeuing
2. **Retry Mechanism:** How RabbitMQ retries failed messages before sending to DLQ
3. **DLQ Message Accumulation:** How failed messages accumulate in the dead letter queue  
4. **Message Visibility:** How to inspect failed messages in RabbitMQ Management UI
5. **Failure Isolation:** How DLQ prevents bad messages from blocking the entire queue
6. **Recovery Strategies:** How to handle messages that end up in DLQ

### Key Differences:
- **Application Failures:** Service catches exceptions → sends "FAILED" message → consumer processes normally
- **Consumer Failures:** Consumer throws exception → message requeued → eventually goes to DLQ
