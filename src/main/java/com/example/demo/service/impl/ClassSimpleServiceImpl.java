package com.example.demo.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.dto.request.ClassRequest;
import com.example.demo.dto.response.ClassResponse;
import com.example.demo.entity.ClassEntity;
import com.example.demo.mapper.request.ClassRequestMapper;
import com.example.demo.mapper.response.ClassResponseMapper;
import com.example.demo.repository.ClassRepository;
import com.example.demo.service.ClassSimpleService;
import com.example.demo.service.RedisService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClassSimpleServiceImpl implements ClassSimpleService {
    private final ClassRepository classRepository;
    private final ClassRequestMapper requestMapper;
    private final ClassResponseMapper responseMapper;
    private final RedisService redisService;
    
    @Override
    public ClassResponse createClass(ClassRequest request) {
        try {
            log.info("SIMPLE - Creating new class with name: {}", request.getName());
            
            // Convert DTO to Entity
            ClassEntity entity = requestMapper.toEntity(request);
            
            // Save to database
            ClassEntity savedEntity = classRepository.save(entity);
            log.info("SIMPLE - Class created successfully with id: {}", savedEntity.getId());
            
            // Convert to response DTO
            ClassResponse response = responseMapper.toDto(savedEntity);
            
            // Cache the new class
            String cacheKey = "simple-class:" + savedEntity.getId();
            redisService.set(cacheKey, response);
            
            // NO RabbitMQ message here - this is the difference!
            log.info("SIMPLE - Class creation completed (NO RabbitMQ) for id: {}", savedEntity.getId());
            return response;
            
        } catch (Exception e) {
            log.error("SIMPLE - Error creating class with name: {}: {}", request.getName(), e.getMessage(), e);
            throw new RuntimeException("Failed to create class", e);
        }
    }
    
    @Override
    public List<ClassResponse> getAllClasses() {
        try {
            log.info("SIMPLE - Fetching all classes");
            List<ClassResponse> classes = responseMapper.toListDto(classRepository.findAll());
            log.info("SIMPLE - Successfully fetched {} classes", classes.size());
            return classes;
        } catch (Exception e) {
            log.error("SIMPLE - Error fetching all classes: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch classes", e);
        }
    }

    @Override
    public ClassResponse getClassById(Integer id) {
        try {
            log.info("SIMPLE - Fetching class with id: {}", id);
            
            // Check cache first
            String cacheKey = "simple-class:" + id;
            ClassResponse cachedClass = redisService.get(cacheKey, ClassResponse.class);
            if (cachedClass != null) {
                log.info("SIMPLE - Cache hit for class with id: {}", id);
                return cachedClass;
            }

            // Fetch from database
            ClassEntity classEntity = classRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Class not found with id: " + id));
            
            ClassResponse response = responseMapper.toDto(classEntity);
            
            // Cache the result
            redisService.set(cacheKey, response);
            log.info("SIMPLE - Class with id: {} cached successfully", id);
            
            return response;
            
        } catch (Exception e) {
            log.error("SIMPLE - Error fetching class with id: {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public ClassResponse updateClass(Integer id, ClassRequest request) {
        try {
            log.info("SIMPLE - Updating class with id: {}", id);
            
            // Check if class exists
            ClassEntity existingEntity = classRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Class not found with id: " + id));
            
            // Update the entity using mapper
            requestMapper.update(existingEntity, request);
            
            // Save updated entity
            ClassEntity updatedEntity = classRepository.save(existingEntity);
            log.info("SIMPLE - Class updated successfully with id: {}", updatedEntity.getId());
            
            // Convert to response DTO
            ClassResponse response = responseMapper.toDto(updatedEntity);
            
            // Update cache
            String cacheKey = "simple-class:" + id;
            redisService.set(cacheKey, response);
            
            // NO RabbitMQ message here!
            log.info("SIMPLE - Class update completed (NO RabbitMQ) for id: {}", updatedEntity.getId());
            
            return response;
            
        } catch (Exception e) {
            log.error("SIMPLE - Error updating class with id: {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public void deleteClass(Integer id) {
        try {
            log.info("SIMPLE - Deleting class with id: {}", id);
            
            // Check if class exists
            ClassEntity existingEntity = classRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Class not found with id: " + id));
            
            String className = existingEntity.getName();
            
            // Delete from database
            classRepository.deleteById(id);
            log.info("SIMPLE - Class deleted successfully with id: {}", id);
            
            // Remove from cache
            String cacheKey = "simple-class:" + id;
            redisService.del(cacheKey);
            
            // NO RabbitMQ message here!
            log.info("SIMPLE - Class deletion completed (NO RabbitMQ) for id: {}", id);
            
        } catch (Exception e) {
            log.error("SIMPLE - Error deleting class with id: {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }
}
