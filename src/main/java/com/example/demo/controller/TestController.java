package com.example.demo.controller;

import java.time.LocalDateTime;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.message.ClassMessage;
import com.example.demo.dto.request.ClassRequest;
import com.example.demo.dto.response.ClassResponse;
import com.example.demo.service.ClassService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/test")
@RequiredArgsConstructor
@Slf4j
public class TestController {
    
    private final ClassService classService;
    private final RabbitTemplate rabbitTemplate;
    
    @PostMapping("/duplicate-key-error")
    public ResponseEntity<?> testDuplicateKeyError() {
        log.info("Testing duplicate key constraint violation");
        
        try {
            // This will intentionally fail with duplicate key error
            ClassRequest request = new ClassRequest();
            request.setName("Test Class - Duplicate Key");
            
            ClassResponse response = classService.createClass(request);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Expected error occurred: {}", e.getMessage());
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
    
    @PostMapping("/null-name-error")
    public ResponseEntity<?> testNullNameError() {
        log.info("Testing null name validation error");
        
        try {
            // This will fail with validation error
            ClassRequest request = new ClassRequest();
            request.setName(null); // Intentionally null
            
            ClassResponse response = classService.createClass(request);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Expected error occurred: {}", e.getMessage());
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
    
    @GetMapping("/nonexistent-class/{id}")
    public ResponseEntity<?> testNonExistentClass(@PathVariable Integer id) {
        log.info("Testing fetch of non-existent class with id: {}", id);
        
        try {
            // Use a very high ID that likely doesn't exist
            ClassResponse response = classService.getClassById(id);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Expected error occurred: {}", e.getMessage());
            return ResponseEntity.status(404).body("Error: " + e.getMessage());
        }
    }
    
    @PutMapping("/update-nonexistent/{id}")
    public ResponseEntity<?> testUpdateNonExistentClass(@PathVariable Integer id) {
        log.info("Testing update of non-existent class with id: {}", id);
        
        try {
            ClassRequest request = new ClassRequest();
            request.setName("Updated Name");
            
            ClassResponse response = classService.updateClass(id, request);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Expected error occurred: {}", e.getMessage());
            return ResponseEntity.status(404).body("Error: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/delete-nonexistent/{id}")
    public ResponseEntity<?> testDeleteNonExistentClass(@PathVariable Integer id) {
        log.info("Testing delete of non-existent class with id: {}", id);
        
        try {
            classService.deleteClass(id);
            return ResponseEntity.ok("Class deleted successfully");
            
        } catch (Exception e) {
            log.error("Expected error occurred: {}", e.getMessage());
            return ResponseEntity.status(404).body("Error: " + e.getMessage());
        }
    }
    
    @PostMapping("/create-with-manual-id")
    public ResponseEntity<?> testCreateWithManualId(@RequestParam(defaultValue = "0") Integer manualId) {
        log.info("Testing class creation with manual ID: {}", manualId);
        
        try {
            ClassRequest request = new ClassRequest();
            request.setName("Manual ID Test - " + manualId);
            
            // Note: Since your entity doesn't auto-generate IDs, 
            // this will likely cause duplicate key errors
            ClassResponse response = classService.createClass(request);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Expected error occurred: {}", e.getMessage());
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
    
    // =============== DLQ TESTING ENDPOINTS ===============
    
    @PostMapping("/dlq/send-poison-message")
    public ResponseEntity<?> sendPoisonMessage() {
        log.info("Sending poison message to trigger DLQ");
        
        try {
            // Create a message that will cause consumer to fail
            ClassMessage poisonMessage = ClassMessage.builder()
                    .classId(-999) // Invalid ID that will cause processing to fail
                    .className("POISON_MESSAGE_TEST")
                    .action("CREATE")
                    .status("SUCCESS")
                    .message("This message is designed to fail in consumer")
                    .timestamp(LocalDateTime.now())
                    .payload("TRIGGER_CONSUMER_FAILURE")
                    .build();
            
            rabbitTemplate.convertAndSend("class.exchange", "class.routing.key", poisonMessage);
            log.info("Poison message sent successfully");
            
            return ResponseEntity.ok("Poison message sent to trigger DLQ");
            
        } catch (Exception e) {
            log.error("Error sending poison message: {}", e.getMessage());
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
    
    @PostMapping("/dlq/send-malformed-message")
    public ResponseEntity<?> sendMalformedMessage() {
        log.info("Sending malformed message to trigger DLQ");
        
        try {
            // Send a message with wrong structure that will cause deserialization failure
            String malformedJson = "{\"invalidField\":\"this will cause json parsing to fail\",\"wrongStructure\":true}";
            
            rabbitTemplate.convertAndSend("class.exchange", "class.routing.key", malformedJson);
            log.info("Malformed message sent successfully");
            
            return ResponseEntity.ok("Malformed message sent to trigger DLQ");
            
        } catch (Exception e) {
            log.error("Error sending malformed message: {}", e.getMessage());
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
    
    @PostMapping("/dlq/send-exception-trigger")
    public ResponseEntity<?> sendExceptionTrigger() {
        log.info("Sending message to trigger runtime exception in consumer");
        
        try {
            ClassMessage exceptionTrigger = ClassMessage.builder()
                    .classId(1)
                    .className("THROW_RUNTIME_EXCEPTION")
                    .action("CREATE")
                    .status("SUCCESS")
                    .message("Consumer should throw RuntimeException when processing this")
                    .timestamp(LocalDateTime.now())
                    .payload("RUNTIME_EXCEPTION_TRIGGER")
                    .build();
            
            rabbitTemplate.convertAndSend("class.exchange", "class.routing.key", exceptionTrigger);
            log.info("Exception trigger message sent successfully");
            
            return ResponseEntity.ok("Exception trigger message sent to DLQ testing");
            
        } catch (Exception e) {
            log.error("Error sending exception trigger: {}", e.getMessage());
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
    
    @PostMapping("/dlq/batch-failure-test")
    public ResponseEntity<?> batchFailureTest(@RequestParam(defaultValue = "5") int messageCount) {
        log.info("Sending {} messages to test DLQ batch processing", messageCount);
        
        try {
            for (int i = 1; i <= messageCount; i++) {
                ClassMessage failureMessage = ClassMessage.builder()
                        .classId(i)
                        .className("BATCH_FAILURE_TEST_" + i)
                        .action("CREATE")
                        .status("SUCCESS")
                        .message("Batch test message " + i)
                        .timestamp(LocalDateTime.now())
                        .payload("BATCH_FAILURE_TRIGGER")
                        .build();
                
                rabbitTemplate.convertAndSend("class.exchange", "class.routing.key", failureMessage);
                Thread.sleep(100); // Small delay between messages
            }
            
            log.info("Sent {} messages for batch DLQ testing", messageCount);
            return ResponseEntity.ok("Sent " + messageCount + " messages for DLQ batch testing");
            
        } catch (Exception e) {
            log.error("Error in batch failure test: {}", e.getMessage());
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
    
    @PostMapping("/test-entity-state")
    public ResponseEntity<?> testEntityStateIssue() {
        log.info("Testing entity state management - should work now after ID mapping fix");
        
        try {
            ClassRequest request = new ClassRequest();
            request.setName("Sinh hoc - Fixed");
            
            log.info("Creating class with request: {}", request);
            ClassResponse response = classService.createClass(request);
            log.info("Successfully created class: {}", response);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Entity state error occurred: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
    
    @PostMapping("/test-update-flow")
    public ResponseEntity<?> testUpdateFlow(@RequestParam Integer classId) {
        log.info("Testing update flow for class ID: {}", classId);
        
        try {
            // First get the class
            ClassResponse existing = classService.getClassById(classId);
            log.info("Found existing class: {}", existing);
            
            // Then update it
            ClassRequest updateRequest = new ClassRequest();
            updateRequest.setName(existing.getName() + " - Updated");
            
            ClassResponse updated = classService.updateClass(classId, updateRequest);
            log.info("Successfully updated class: {}", updated);
            
            return ResponseEntity.ok(updated);
            
        } catch (Exception e) {
            log.error("Update flow error: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}
