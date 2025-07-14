package com.example.demo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.APIResponse;
import com.example.demo.dto.request.ClassRequest;
import com.example.demo.dto.response.ClassResponse;
import com.example.demo.service.ClassSimpleService;

import io.micrometer.common.lang.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/classes-simple")
@Slf4j
public class ClassSimpleController {
    private final ClassSimpleService classSimpleService;
    
    /**
     * Get all classes (Simple - No RabbitMQ)
     * GET /api/v1/classes-simple
     */
    @GetMapping
    public ResponseEntity<APIResponse> getAllClasses() {
        try {
            log.info("SIMPLE CONTROLLER - Fetching all classes");
            List<ClassResponse> classList = classSimpleService.getAllClasses();
            
            APIResponse response = APIResponse.builder()
                    .statusCode(200)
                    .message("SIMPLE - List of classes retrieved successfully")
                    .data(classList)
                    .build();
            
            log.info("SIMPLE CONTROLLER - Successfully retrieved {} classes", classList.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("SIMPLE CONTROLLER - Error fetching classes: {}", e.getMessage(), e);
            APIResponse errorResponse = APIResponse.builder()
                    .statusCode(500)
                    .message("SIMPLE - Failed to fetch classes: " + e.getMessage())
                    .data(null)
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get class by ID (Simple - No RabbitMQ)
     * GET /api/v1/classes-simple/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<APIResponse> getClassById(@PathVariable("id") @NonNull Integer id) {
        try {
            log.info("SIMPLE CONTROLLER - Fetching class with id: {}", id);
            ClassResponse classResponse = classSimpleService.getClassById(id);
            
            APIResponse response = APIResponse.builder()
                    .statusCode(200)
                    .message("SIMPLE - Class retrieved successfully")
                    .data(classResponse)
                    .build();
            
            log.info("SIMPLE CONTROLLER - Successfully retrieved class with id: {}", id);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("SIMPLE CONTROLLER - Error fetching class with id {}: {}", id, e.getMessage(), e);
            APIResponse errorResponse = APIResponse.builder()
                    .statusCode(e.getMessage().contains("not found") ? 404 : 500)
                    .message("SIMPLE - Failed to fetch class: " + e.getMessage())
                    .data(null)
                    .build();
            
            HttpStatus status = e.getMessage().contains("not found") ? 
                HttpStatus.NOT_FOUND : HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).body(errorResponse);
        }
    }
    
    /**
     * Create a new class (Simple - NO RabbitMQ message)
     * POST /api/v1/classes-simple
     */
    @PostMapping
    public ResponseEntity<APIResponse> createClass(@RequestBody ClassRequest request) {
        try {
            long startTime = System.currentTimeMillis();
            log.info("SIMPLE CONTROLLER - Creating class: {}", request.getName());
            
            ClassResponse createdClass = classSimpleService.createClass(request);
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            APIResponse response = APIResponse.builder()
                    .statusCode(201)
                    .message("SIMPLE - Class created successfully (NO RabbitMQ) in " + duration + "ms")
                    .data(createdClass)
                    .build();
            
            log.info("SIMPLE CONTROLLER - Class created in {}ms (NO RabbitMQ)", duration);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            log.error("SIMPLE CONTROLLER - Error creating class: {}", e.getMessage(), e);
            APIResponse errorResponse = APIResponse.builder()
                    .statusCode(500)
                    .message("SIMPLE - Failed to create class: " + e.getMessage())
                    .data(null)
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Update class by ID (Simple - NO RabbitMQ message)
     * PUT /api/v1/classes-simple/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<APIResponse> updateClass(@PathVariable Integer id, @RequestBody ClassRequest request) {
        try {
            long startTime = System.currentTimeMillis();
            log.info("SIMPLE CONTROLLER - Updating class with id: {}", id);
            
            ClassResponse updatedClass = classSimpleService.updateClass(id, request);
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            APIResponse response = APIResponse.builder()
                    .statusCode(200)
                    .message("SIMPLE - Class updated successfully (NO RabbitMQ) in " + duration + "ms")
                    .data(updatedClass)
                    .build();
            
            log.info("SIMPLE CONTROLLER - Class updated in {}ms (NO RabbitMQ)", duration);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("SIMPLE CONTROLLER - Error updating class: {}", e.getMessage(), e);
            APIResponse errorResponse = APIResponse.builder()
                    .statusCode(e.getMessage().contains("not found") ? 404 : 500)
                    .message("SIMPLE - Failed to update class: " + e.getMessage())
                    .data(null)
                    .build();
            
            HttpStatus status = e.getMessage().contains("not found") ? 
                HttpStatus.NOT_FOUND : HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).body(errorResponse);
        }
    }
    
    /**
     * Delete class by ID (Simple - NO RabbitMQ message)
     * DELETE /api/v1/classes-simple/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<APIResponse> deleteClass(@PathVariable Integer id) {
        try {
            long startTime = System.currentTimeMillis();
            log.info("SIMPLE CONTROLLER - Deleting class with id: {}", id);
            
            classSimpleService.deleteClass(id);
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            APIResponse response = APIResponse.builder()
                    .statusCode(200)
                    .message("SIMPLE - Class deleted successfully (NO RabbitMQ) in " + duration + "ms")
                    .data(null)
                    .build();
            
            log.info("SIMPLE CONTROLLER - Class deleted in {}ms (NO RabbitMQ)", duration);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("SIMPLE CONTROLLER - Error deleting class: {}", e.getMessage(), e);
            APIResponse errorResponse = APIResponse.builder()
                    .statusCode(e.getMessage().contains("not found") ? 404 : 500)
                    .message("SIMPLE - Failed to delete class: " + e.getMessage())
                    .data(null)
                    .build();
            
            HttpStatus status = e.getMessage().contains("not found") ? 
                HttpStatus.NOT_FOUND : HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).body(errorResponse);
        }
    }
}
