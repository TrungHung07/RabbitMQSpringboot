# PowerShell Commands for RabbitMQ Testing

## 1. Test Class Creation (PowerShell Syntax)
```powershell
# Method 1: Using Invoke-RestMethod (Recommended)
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/classes" -Method POST -Headers @{"Content-Type"="application/json"} -Body '{"name": "Test Queue Check"}'

# Method 2: Using Invoke-WebRequest
$headers = @{"Content-Type" = "application/json"}
$body = '{"name": "Test Queue Check"}'
Invoke-WebRequest -Uri "http://localhost:8080/api/v1/classes" -Method POST -Headers $headers -Body $body
```

## 2. Test DLQ Endpoints
```powershell
# Test poison message (should go to DLQ)
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/test/dlq/send-poison-message" -Method POST

# Test exception trigger
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/test/dlq/send-exception-trigger" -Method POST

# Test batch failure
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/test/dlq/batch-failure-test?messageCount=3" -Method POST
```

## 3. Check Application Status
```powershell
# Check if Spring Boot is running
Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -Method GET

# Get all classes
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/classes" -Method GET
```

## 4. Direct RabbitMQ Testing
```powershell
# Test direct message sending (bypasses service layer)
$rabbitTestBody = @{
    classId = 999
    className = "Direct Test"
    action = "CREATE" 
    status = "SUCCESS"
    message = "Direct RabbitMQ test"
    timestamp = (Get-Date).ToString("yyyy-MM-ddTHH:mm:ss")
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/v1/test/dlq/send-poison-message" -Method POST
```

## Troubleshooting Empty Queue Issue

### Possible Reasons for Empty Queue:

1. **Messages are consumed immediately** (normal behavior)
2. **Consumer is not running** 
3. **Messages are not being produced**
4. **Configuration issues**

### Diagnostic Steps:

#### Step 1: Check if messages are being produced
```powershell
# This should show in Spring Boot logs if RabbitMQ messages are sent
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/classes" -Method POST -Headers @{"Content-Type"="application/json"} -Body '{"name": "Debug Test"}'
```

#### Step 2: Check Spring Boot logs
Look for these log patterns:
- `Publishing class message: ClassMessage(...)`
- `Successfully published class message for class ID: {}`
- `Received class message: ClassMessage(...)`

#### Step 3: Temporarily disable consumer to see messages accumulate
You can comment out the @RabbitListener annotation temporarily to see messages pile up in the queue.

#### Step 4: Use DLQ tests to force message retention
```powershell
# These messages should stay in DLQ for inspection
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/test/dlq/send-poison-message" -Method POST
```

#### Step 5: Check RabbitMQ connection
```powershell
# Check Docker containers
docker ps

# Check RabbitMQ logs
docker logs <rabbitmq-container-id>
```

## Expected Behavior Analysis

### Normal Flow (Queue appears empty):
1. Message sent to `class.queue`
2. Consumer immediately processes message
3. Message acknowledged and removed
4. Queue shows as empty (this is GOOD!)

### DLQ Flow (Messages should accumulate):
1. Poison message sent to `class.queue`
2. Consumer fails to process
3. Message retried multiple times
4. Message moved to `class.queue.dlq`
5. DLQ should show messages

## Verification Commands

```powershell
# 1. Test normal creation
Write-Host "Testing normal class creation..."
$result1 = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/classes" -Method POST -Headers @{"Content-Type"="application/json"} -Body '{"name": "Normal Test"}'
Write-Host "Result: $($result1 | ConvertTo-Json)"

# 2. Test DLQ
Write-Host "Testing DLQ message..."
$result2 = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/test/dlq/send-poison-message" -Method POST
Write-Host "Result: $result2"

# 3. Wait and check queues
Write-Host "Wait 10 seconds then check RabbitMQ UI for messages in class.queue.dlq"
Start-Sleep -Seconds 10
```
