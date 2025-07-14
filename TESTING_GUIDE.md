# Complete RabbitMQ Flow Testing Guide

## 🚀 **How to Test the Complete Flow**

### **Prerequisites:**
1. Start your services:
```bash
# Start RabbitMQ and other services
docker-compose up -d

# Start your Spring Boot application
mvn spring-boot:run
```

2. Verify services are running:
- **RabbitMQ Management**: http://localhost:15672 (admin/password)
- **Your App**: http://localhost:8080
- **PostgreSQL**: localhost:5432
- **Redis**: localhost:6379

---

## 📋 **Postman API Testing**

### **1. CREATE Class (Triggers RabbitMQ Message)**
```
POST http://localhost:8080/api/v1/classes
Content-Type: application/json

{
    "name": "Mathematics 101"
}
```

**Expected Response:**
```json
{
    "statusCode": 201,
    "message": "Class created successfully",
    "data": {
        "id": 1,
        "name": "Mathematics 101"
    }
}
```

**What Happens:**
1. ✅ Class saved to PostgreSQL
2. ✅ Class cached in Redis
3. ✅ RabbitMQ message sent to `class.queue`
4. ✅ Message consumed and logged

---

### **2. GET All Classes**
```
GET http://localhost:8080/api/v1/classes
```

**Expected Response:**
```json
{
    "statusCode": 200,
    "message": "List of classes retrieved successfully",
    "data": [
        {
            "id": 1,
            "name": "Mathematics 101"
        }
    ]
}
```

---

### **3. GET Class by ID (Uses Cache)**
```
GET http://localhost:8080/api/v1/classes/1
```

**First call**: Fetches from database, caches result
**Second call**: Returns from Redis cache (faster)

---

### **4. UPDATE Class (Triggers RabbitMQ Message)**
```
PUT http://localhost:8080/api/v1/classes/1
Content-Type: application/json

{
    "name": "Advanced Mathematics 101"
}
```

**What Happens:**
1. ✅ Class updated in PostgreSQL
2. ✅ Cache updated in Redis
3. ✅ RabbitMQ UPDATE message sent
4. ✅ Message consumed and logged

---

### **5. DELETE Class (Triggers RabbitMQ Message)**
```
DELETE http://localhost:8080/api/v1/classes/1
```

**What Happens:**
1. ✅ Class deleted from PostgreSQL
2. ✅ Cache removed from Redis
3. ✅ RabbitMQ DELETE message sent
4. ✅ Message consumed and logged

---

## 🐰 **Monitor RabbitMQ Messages**

### **1. Access RabbitMQ Management UI:**
- URL: http://localhost:15672
- Username: `admin`
- Password: `password`

### **2. Check Queues:**
- Go to **Queues** tab
- You should see:
  - `class.queue` - Main queue
  - `class.dlq` - Dead letter queue

### **3. Monitor Messages:**
- Click on `class.queue`
- See message rates and totals
- Use "Get Messages" to peek at message content

---

## 📊 **Check Application Logs**

Look for these log messages in your Spring Boot console:

### **When Creating a Class:**
```
INFO  - Creating new class with name: Mathematics 101
INFO  - Class created successfully with id: 1
INFO  - Class creation notification sent to RabbitMQ for id: 1
INFO  - Publishing class message: ClassMessage(classId=1, className=Mathematics 101, action=CREATE, status=SUCCESS, ...)
INFO  - Successfully published class message for class ID: 1
INFO  - Received class message: ClassMessage(classId=1, className=Mathematics 101, action=CREATE, ...)
INFO  - Processing class creation for ID: 1, Name: Mathematics 101
INFO  - Class 'Mathematics 101' with ID 1 has been created successfully
```

### **When Getting a Class (Cache Hit):**
```
INFO  - Fetching class with id: 1
INFO  - Cache hit for class with id: 1
```

---

## 🧪 **Test Error Scenarios**

### **1. Test 404 - Class Not Found:**
```
GET http://localhost:8080/api/v1/classes/999
```

**Expected Response:**
```json
{
    "statusCode": 404,
    "message": "Failed to fetch class: Class not found with id: 999",
    "data": null
}
```

### **2. Test RabbitMQ Dead Letter Queue:**
- Stop the message consumer temporarily
- Send multiple messages
- Messages will go to DLQ after TTL expires

---

## ✅ **Complete Flow Verification**

1. **Create a class** → Check PostgreSQL, Redis, RabbitMQ logs
2. **Get the class** → Should use cache on second request
3. **Update the class** → Check all systems updated
4. **Delete the class** → Verify cleanup everywhere

---

## 🔍 **Database Verification**

Connect to PostgreSQL and check:
```sql
-- Check if class was created
SELECT * FROM classes;

-- Check Redis keys (if you have Redis CLI)
-- redis-cli
-- KEYS class:*
-- GET class:1
```

---

## 🎯 **Success Indicators**

You'll know everything is working when you see:

1. ✅ **API responses** are correct
2. ✅ **Database records** are created/updated/deleted
3. ✅ **Redis cache** is working (faster subsequent requests)
4. ✅ **RabbitMQ messages** are published and consumed
5. ✅ **Console logs** show the complete flow
6. ✅ **RabbitMQ UI** shows message activity

**Your complete RabbitMQ integration is now ready! 🎉**
