# StaleObjectStateException Fix Guide

## Problem Analysis

### What Happened:
You encountered a `org.hibernate.StaleObjectStateException` when trying to create a class with the request:
```json
{ "id": 10, "name": "Sinh hoc" }
```

### Root Cause:
1. **ID in Request DTO**: Your `ClassRequest` had an `id` field
2. **Hibernate Confusion**: When you send an ID in the request, Hibernate thinks you're trying to update an existing entity
3. **Entity State Mismatch**: Hibernate couldn't determine if this was a new entity or an existing one to merge

### The Error Chain:
```
ClassRequest with ID → Mapper sets ID on Entity → Hibernate tries to merge → 
Can't find entity with that ID → StaleObjectStateException
```

## Solution Applied

### 1. Removed ID from ClassRequest
**Before:**
```java
public class ClassRequest {
    private int id;      // ❌ This caused the problem
    private String name;
}
```

**After:**
```java
public class ClassRequest {
    private String name; // ✅ Only business data, no ID
}
```

### 2. Updated Mapper to Ignore ID
**Enhanced ClassRequestMapper:**
```java
@Mapper(componentModel = "spring")
public interface ClassRequestMapper extends BaseRequestMapper<ClassRequest, ClassEntity> {
    
    @Override
    @Mapping(target = "id", ignore = true) // Never map ID from request
    ClassEntity toEntity(ClassRequest dto);
    
    @Override
    @Mapping(target = "id", ignore = true) // Never update ID during updates
    void update(@MappingTarget ClassEntity entity, ClassRequest dto);
}
```

## Testing the Fix

### 1. Test Entity State Management
```bash
curl -X POST http://localhost:8080/api/v1/test/test-entity-state
```
**Expected:** Should create class successfully without StaleObjectStateException

### 2. Test Update Flow
```bash
# First create a class
curl -X POST http://localhost:8080/api/v1/classes \
  -H "Content-Type: application/json" \
  -d '{"name": "Test Class"}'

# Then test update (replace {id} with actual ID from response)
curl -X POST "http://localhost:8080/api/v1/test/test-update-flow?classId={id}"
```

### 3. Test Normal Operations
```bash
# Create without ID in request
curl -X POST http://localhost:8080/api/v1/classes \
  -H "Content-Type: application/json" \
  -d '{"name": "Sinh hoc"}'
```

## What Changed in Your Workflow

### Before (Problematic):
```json
POST /api/v1/classes
{
  "id": 10,
  "name": "Sinh hoc"
}
```
→ StaleObjectStateException

### After (Fixed):
```json
POST /api/v1/classes
{
  "name": "Sinh hoc"
}
```
→ Success with auto-generated ID

## Best Practices Applied

### 1. Separation of Concerns
- **Create Requests**: No ID field (auto-generated)
- **Update Requests**: ID in URL path, not request body
- **Response DTOs**: Include ID for client reference

### 2. Proper REST API Design
```bash
# Create (POST) - ID auto-generated
POST /api/v1/classes
{"name": "New Class"}

# Update (PUT) - ID in URL
PUT /api/v1/classes/{id}
{"name": "Updated Name"}

# Get (GET) - ID in URL
GET /api/v1/classes/{id}
```

### 3. Entity State Management
- Let Hibernate manage entity lifecycle
- Don't mix create/update logic
- Use explicit mapping configurations

## Prevention for Future

### 1. DTO Design Rules
- **Create DTOs**: No ID fields
- **Update DTOs**: No ID fields (use path parameters)
- **Response DTOs**: Include all fields including ID

### 2. Mapper Configurations
- Always explicitly handle ID mapping
- Use `@Mapping(target = "id", ignore = true)` for create operations
- Be explicit about what fields to map/ignore

### 3. Testing Strategy
- Test entity state scenarios
- Verify create/update operations separately
- Include edge cases in test suite

## Verification Steps

1. **Restart your application** to pick up the mapper changes
2. **Test creation** without ID in request body
3. **Test updates** using path parameters for ID
4. **Verify in database** that IDs are auto-generated correctly

The StaleObjectStateException should now be resolved, and your entity state management will be consistent and predictable.
