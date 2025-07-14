package com.example.demo.service;

import java.util.List;

import com.example.demo.dto.request.ClassRequest;
import com.example.demo.dto.response.ClassResponse;

/**
 * Simple Class Service without RabbitMQ messaging
 * For performance comparison testing
 */
public interface ClassSimpleService {
    // Create operations
    ClassResponse createClass(ClassRequest request);
    
    // Read operations
    List<ClassResponse> getAllClasses();
    ClassResponse getClassById(Integer id);
    
    // Update operations
    ClassResponse updateClass(Integer id, ClassRequest request);
    
    // Delete operations
    void deleteClass(Integer id);
}
