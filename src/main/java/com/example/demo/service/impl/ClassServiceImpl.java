package com.example.demo.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.dto.request.ClassRequest;
import com.example.demo.dto.response.ClassResponse;
import com.example.demo.entity.ClassEntity;
import com.example.demo.mapper.request.ClassRequestMapper;
import com.example.demo.mapper.response.ClassResponseMapper;
import com.example.demo.repository.ClassRepository;
import com.example.demo.service.ClassService;
import com.example.demo.service.RedisService;
import com.example.demo.service.messaging.ClassMessagingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClassServiceImpl implements ClassService {
    private final ClassRepository classRepository;
    private final ClassRequestMapper requestMapper;
    private final ClassResponseMapper responseMapper;
    private final RedisService redisService;
    private final ClassMessagingService messagingService;
    
    @Override
    public ClassResponse createClass(ClassRequest request) {
        try {
            log.info("Creating new class with name: {}", request.getName());
            
            // Convert DTO to Entity
            ClassEntity entity = requestMapper.toEntity(request);
            
            // Save to database
            ClassEntity savedEntity = classRepository.save(entity);
            log.info("Class created successfully with id: {}", savedEntity.getId());
            
            // Convert to response DTO
            ClassResponse response = responseMapper.toDto(savedEntity);
            
            // Cache the new class
            String cacheKey = "class:" + savedEntity.getId();
            redisService.set(cacheKey, response);
            
            // Send RabbitMQ message
            messagingService.notifyClassCreated(savedEntity.getId(), savedEntity.getName());
            log.info("Class creation notification sent to RabbitMQ for id: {}", savedEntity.getId());
            
            return response;
            
        } catch (Exception e) {
            log.error("Error creating class with name: {}: {}", request.getName(), e.getMessage(), e);
            messagingService.notifyClassOperationFailed(null, request.getName(), "CREATE", e.getMessage());
            throw new RuntimeException("Failed to create class", e);
        }
    }
    
    @Override
    public List<ClassResponse> getAllClasses() {
        try {
            log.info("Fetching all classes");
            List<ClassResponse> classes = responseMapper.toListDto(classRepository.findAll());
            log.info("Successfully fetched {} classes", classes.size());
            return classes;
        } catch (Exception e) {
            log.error("Error fetching all classes: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch classes", e);
        }
    }

    @Override
    public ClassResponse getClassById(Integer id) {
        try {
            log.info("Fetching class with id: {}", id);
            
            // Check cache first
            String cacheKey = "class:" + id;
            ClassResponse cachedClass = redisService.get(cacheKey, ClassResponse.class);
            if (cachedClass != null) {
                log.info("Cache hit for class with id: {}", id);
                return cachedClass;
            }

            // Fetch from database
            ClassEntity classEntity = classRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Class not found with id: " + id));
            
            ClassResponse response = responseMapper.toDto(classEntity);
            
            // Cache the result
            redisService.set(cacheKey, response);
            log.info("Class with id: {} cached successfully", id);
            
            return response;
            
        } catch (Exception e) {
            log.error("Error fetching class with id: {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public ClassResponse updateClass(Integer id, ClassRequest request) {
        try {
            log.info("Updating class with id: {}", id);
            
            // Check if class exists
            ClassEntity existingEntity = classRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Class not found with id: " + id));
            
            // Update the entity using mapper
            requestMapper.update(existingEntity, request);
            
            // Save updated entity
            ClassEntity updatedEntity = classRepository.save(existingEntity);
            log.info("Class updated successfully with id: {}", updatedEntity.getId());
            
            // Convert to response DTO
            ClassResponse response = responseMapper.toDto(updatedEntity);
            
            // Update cache
            String cacheKey = "class:" + id;
            redisService.set(cacheKey, response);
            
            // Send RabbitMQ message
            messagingService.notifyClassUpdated(updatedEntity.getId(), updatedEntity.getName());
            log.info("Class update notification sent to RabbitMQ for id: {}", updatedEntity.getId());
            
            return response;
            
        } catch (Exception e) {
            log.error("Error updating class with id: {}: {}", id, e.getMessage(), e);
            messagingService.notifyClassOperationFailed(id, request.getName(), "UPDATE", e.getMessage());
            throw e;
        }
    }
    
    @Override
    public void deleteClass(Integer id) {
        try {
            log.info("Deleting class with id: {}", id);
            
            // Check if class exists
            ClassEntity existingEntity = classRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Class not found with id: " + id));
            
            String className = existingEntity.getName();
            
            // Delete from database
            classRepository.deleteById(id);
            log.info("Class deleted successfully with id: {}", id);
            
            // Remove from cache
            String cacheKey = "class:" + id;
            redisService.del(cacheKey);
            
            // Send RabbitMQ message
            messagingService.notifyClassDeleted(id, className);
            log.info("Class deletion notification sent to RabbitMQ for id: {}", id);
            
        } catch (Exception e) {
            log.error("Error deleting class with id: {}: {}", id, e.getMessage(), e);
            messagingService.notifyClassOperationFailed(id, null, "DELETE", e.getMessage());
            throw e;
        }
    }
    
    
}
