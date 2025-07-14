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
import com.example.demo.service.ClassService;

import io.micrometer.common.lang.NonNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/classes")
public class ClassController {
    private final ClassService classService;
    
    @GetMapping("")
    public ResponseEntity<APIResponse> GetList() {
        List<ClassResponse> classList = classService.getAllClasses();
        APIResponse response = APIResponse.builder()
                                          .statusCode(200)
                                          .message("List of classes retrieved successfully")
                                          .data(classList)
                                          .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<APIResponse> GetById(@PathVariable("id") @NonNull Integer id) {
        ClassResponse classResponse = classService.getClassById(id);
        APIResponse response = APIResponse.builder()
                                          .statusCode(200)
                                          .message("Class retrieved successfully")
                                          .data(classResponse)
                                          .build();
        return ResponseEntity.ok(response);
    }
    
    /**
     * Create a new class - This will trigger RabbitMQ message
     * POST /api/v1/classes
     */
    @PostMapping
    public ResponseEntity<APIResponse> createClass(@RequestBody ClassRequest request) {
        try {
            ClassResponse createdClass = classService.createClass(request);
            
            APIResponse response = APIResponse.builder()
                    .statusCode(201)
                    .message("Class created successfully")
                    .data(createdClass)
                    .build();
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            APIResponse errorResponse = APIResponse.builder()
                    .statusCode(500)
                    .message("Failed to create class: " + e.getMessage())
                    .data(null)
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Update class by ID - This will trigger RabbitMQ message
     * PUT /api/v1/classes/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<APIResponse> updateClass(@PathVariable Integer id, @RequestBody ClassRequest request) {
        try {
            ClassResponse updatedClass = classService.updateClass(id, request);
            
            APIResponse response = APIResponse.builder()
                    .statusCode(200)
                    .message("Class updated successfully")
                    .data(updatedClass)
                    .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            APIResponse errorResponse = APIResponse.builder()
                    .statusCode(e.getMessage().contains("not found") ? 404 : 500)
                    .message("Failed to update class: " + e.getMessage())
                    .data(null)
                    .build();
            
            HttpStatus status = e.getMessage().contains("not found") ? 
                HttpStatus.NOT_FOUND : HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).body(errorResponse);
        }
    }
    
    /**
     * Delete class by ID - This will trigger RabbitMQ message
     * DELETE /api/v1/classes/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<APIResponse> deleteClass(@PathVariable Integer id) {
        try {
            classService.deleteClass(id);
            
            APIResponse response = APIResponse.builder()
                    .statusCode(200)
                    .message("Class deleted successfully")
                    .data(null)
                    .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            APIResponse errorResponse = APIResponse.builder()
                    .statusCode(e.getMessage().contains("not found") ? 404 : 500)
                    .message("Failed to delete class: " + e.getMessage())
                    .data(null)
                    .build();
            
            HttpStatus status = e.getMessage().contains("not found") ? 
                HttpStatus.NOT_FOUND : HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).body(errorResponse);
        }
    }
}
