# RabbitMQ UI Monitoring Guide

## Quick Access
- **URL:** http://localhost:15672
- **Credentials:** guest/guest

## Monitoring Your Queues

### 1. Main Queue: `class.queue`
**Purpose:** Receives all class operation messages
**Location:** Queues tab → click "class.queue"

**Key Metrics to Watch:**
- **Ready:** Messages waiting for processing
- **Unacked:** Messages being processed
- **Total:** All messages
- **Rate:** Messages per second

**Message Inspection:**
1. Scroll to "Get messages" section
2. Set Messages: 10
3. Set Ackmode: "Nack message requeue true"
4. Click "Get Message(s)"

### 2. Dead Letter Queue: `class.queue.dlq`
**Purpose:** Stores messages that failed processing
**Location:** Queues tab → click "class.queue.dlq"

**When to Check:**
- After running DLQ test endpoints
- When consumer throws exceptions
- To debug failed message processing

### 3. Exchange Monitoring: `class.exchange`
**Purpose:** Routes messages to appropriate queues
**Location:** Exchanges tab → click "class.exchange"

**What to Check:**
- **Message rates in/out**
- **Bindings:** Shows connected queues
- **Message routing statistics**

## Live Testing Workflow

### Step 1: Open Multiple Browser Tabs
```
Tab 1: RabbitMQ UI - Queues overview
Tab 2: RabbitMQ UI - class.queue details  
Tab 3: RabbitMQ UI - class.queue.dlq details
Tab 4: Your application logs
```

### Step 2: Test Normal Operations
```bash
# Create a class - watch messages flow
curl -X POST http://localhost:8080/api/v1/classes \
  -H "Content-Type: application/json" \
  -d '{"name": "Test Class"}'
```

**Expected in UI:**
- `class.queue`: Message count increases briefly, then decreases (consumed)
- Message rates show activity
- No messages in DLQ

### Step 3: Test DLQ Scenarios
```bash
# Trigger consumer failure
curl -X POST http://localhost:8080/api/v1/test/dlq/send-poison-message
```

**Expected in UI:**
- `class.queue`: Message appears, gets retried
- `class.queue.dlq`: Failed message appears after retries
- Channel shows unacked messages during retries

### Step 4: Message Content Inspection

**For Success Messages:**
```json
{
  "routing_key": "class.routing.key",
  "properties": {
    "content_type": "application/json"
  },
  "payload": {
    "classId": 1,
    "className": "Math Class", 
    "action": "CREATE",
    "status": "SUCCESS"
  }
}
```

**For DLQ Messages:**
```json
{
  "routing_key": "class.queue.dlq",
  "properties": {
    "content_type": "application/json"
  },
  "payload": {
    "classId": -999,
    "className": "POISON_MESSAGE_TEST",
    "action": "CREATE", 
    "status": "SUCCESS",
    "payload": "TRIGGER_CONSUMER_FAILURE"
  }
}
```

## Advanced Monitoring Features

### Message Tracking
1. **Go to message details**
2. **Check routing information**
3. **View headers and properties**
4. **See delivery information**

### Queue Statistics
- **Message rates over time**
- **Consumer utilization**
- **Memory usage**
- **Disk usage**

### Channel Monitoring
- **Prefetch count:** How many unacked messages per consumer
- **Consumer tags:** Identify specific consumers
- **Message rates per channel**

## Troubleshooting Common Issues

### No Messages Appearing
- Check if RabbitMQ is running: `docker ps`
- Verify Spring Boot connection logs
- Check exchange bindings are correct

### Messages Stuck in Queue
- Check if consumers are running
- Look for consumer errors in application logs
- Verify consumer acknowledgments

### DLQ Not Working
- Verify DLQ configuration in RabbitMQConfig
- Check if consumers are throwing exceptions
- Ensure retry logic is configured

### Performance Monitoring
- **Message throughput:** Check rates in overview
- **Memory usage:** Monitor node resources
- **Connection count:** Verify app connections

## Quick Commands for Testing

```bash
# Test normal flow
curl -X POST http://localhost:8080/api/v1/classes -H "Content-Type: application/json" -d '{"name": "Normal Class"}'

# Test DLQ flow  
curl -X POST http://localhost:8080/api/v1/test/dlq/send-poison-message

# Test batch DLQ
curl -X POST "http://localhost:8080/api/v1/test/dlq/batch-failure-test?messageCount=5"

# Check application health
curl http://localhost:8080/actuator/health
```

## Pro Tips

1. **Refresh frequently** - UI doesn't auto-refresh
2. **Use "Nack requeue true"** when inspecting to keep messages
3. **Monitor during testing** - watch real-time changes
4. **Check both queue and DLQ** - understand message flow
5. **Look at message properties** - debugging info is in headers
