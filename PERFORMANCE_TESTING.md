# Performance Comparison Testing Guide

## 🚀 **Two Different Endpoints for Speed Testing**

### **WITH RabbitMQ (Original):**
- **Base URL**: `/api/v1/classes`
- **Features**: Database + Redis + RabbitMQ messaging
- **Use Case**: Production-ready with event-driven architecture

### **WITHOUT RabbitMQ (Simple):**
- **Base URL**: `/api/v1/classes-simple`
- **Features**: Database + Redis only (NO messaging)
- **Use Case**: Performance comparison testing

---

## 📊 **Performance Testing URLs**

### **1. CREATE Class Comparison:**

**WITH RabbitMQ:**
```
POST http://localhost:8080/api/v1/classes
Content-Type: application/json

{
    "name": "Math with RabbitMQ"
}
```

**WITHOUT RabbitMQ:**
```
POST http://localhost:8080/api/v1/classes-simple
Content-Type: application/json

{
    "name": "Math without RabbitMQ"
}
```

### **2. UPDATE Class Comparison:**

**WITH RabbitMQ:**
```
PUT http://localhost:8080/api/v1/classes/1
Content-Type: application/json

{
    "name": "Updated Math with RabbitMQ"
}
```

**WITHOUT RabbitMQ:**
```
PUT http://localhost:8080/api/v1/classes-simple/1
Content-Type: application/json

{
    "name": "Updated Math without RabbitMQ"
}
```

### **3. DELETE Class Comparison:**

**WITH RabbitMQ:**
```
DELETE http://localhost:8080/api/v1/classes/1
```

**WITHOUT RabbitMQ:**
```
DELETE http://localhost:8080/api/v1/classes-simple/1
```

### **4. GET Operations (Should be similar speed):**

**WITH RabbitMQ:**
```
GET http://localhost:8080/api/v1/classes
GET http://localhost:8080/api/v1/classes/1
```

**WITHOUT RabbitMQ:**
```
GET http://localhost:8080/api/v1/classes-simple
GET http://localhost:8080/api/v1/classes-simple/1
```

---

## ⏱️ **Performance Testing Method**

### **Postman Performance Testing:**
1. **Create Collection** with both endpoints
2. **Use Collection Runner** with iterations
3. **Compare response times** in the results

### **Manual Timing:**
1. **Check Response Messages** - both controllers show execution time
2. **Monitor Console Logs** - look for timing information
3. **Use Browser DevTools** - Network tab for request duration

---

## 📈 **Expected Results**

### **WITH RabbitMQ (Slower but Feature-Rich):**
- ✅ Database operation
- ✅ Redis caching
- ✅ RabbitMQ message publishing
- ✅ Message consumption
- ✅ Event-driven architecture
- **Expected Time**: ~100-300ms

### **WITHOUT RabbitMQ (Faster but Basic):**
- ✅ Database operation
- ✅ Redis caching
- ❌ NO messaging
- ❌ NO event notifications
- **Expected Time**: ~50-150ms

---

## 🧪 **Testing Scenarios**

### **Scenario 1: Single Operations**
```bash
# Test individual requests
POST /api/v1/classes
POST /api/v1/classes-simple
```

### **Scenario 2: Bulk Operations**
```bash
# Create multiple classes rapidly
# Use Postman Collection Runner with 10-100 iterations
```

### **Scenario 3: Load Testing**
```bash
# Use tools like Apache Bench (ab) or Artillery
ab -n 100 -c 10 -H "Content-Type: application/json" -p data.json http://localhost:8080/api/v1/classes
ab -n 100 -c 10 -H "Content-Type: application/json" -p data.json http://localhost:8080/api/v1/classes-simple
```

---

## 📋 **Console Log Comparison**

### **WITH RabbitMQ Logs:**
```
Creating new class with name: Test
Class created successfully with id: 1
Class creation notification sent to RabbitMQ for id: 1
Publishing class message: ClassMessage(...)
Successfully published class message for class ID: 1
Received class message: ClassMessage(...)
Processing class creation for ID: 1
```

### **WITHOUT RabbitMQ Logs:**
```
SIMPLE - Creating new class with name: Test
SIMPLE - Class created successfully with id: 1
SIMPLE - Class creation completed (NO RabbitMQ) for id: 1
SIMPLE CONTROLLER - Class created in 45ms (NO RabbitMQ)
```

---

## 🎯 **What to Compare**

1. **Response Time** - Check API response duration
2. **Console Logs** - Compare log volume and timing
3. **Resource Usage** - Monitor CPU/Memory during operations
4. **Throughput** - How many operations per second
5. **RabbitMQ Activity** - Only the full version will show queue activity

---

## 📝 **Testing Checklist**

- [ ] Start Docker services: `docker-compose up -d`
- [ ] Start Spring Boot application
- [ ] Test simple endpoint first: `POST /api/v1/classes-simple`
- [ ] Test full endpoint: `POST /api/v1/classes`
- [ ] Compare response times in Postman
- [ ] Check console logs for performance differences
- [ ] Monitor RabbitMQ UI for message activity
- [ ] Run bulk tests with Collection Runner

**Now you can measure the exact performance impact of RabbitMQ messaging! 🚀**
