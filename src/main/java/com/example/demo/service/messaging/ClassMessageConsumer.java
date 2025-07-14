package com.example.demo.service.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.example.demo.dto.message.ClassMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClassMessageConsumer {
    
    /**
     * Listens to class messages from RabbitMQ
     * 
     * @param message The received class message
     */
    @RabbitListener(queues = "${app.rabbitmq.class.queue.name}")
    public void handleClassMessage(ClassMessage message) {
        try {
            log.info("Received class message: {}", message);
            
            // DLQ Testing Triggers
            if ("TEST_FAILURE_TRIGGER".equals(message.getMessage())) {
                log.error("TEST MODE: Simulating message processing failure for class ID: {}", message.getClassId());
                throw new RuntimeException("Simulated failure for DLQ testing - Class ID: " + message.getClassId());
            }
            
            // Check for poison message test
            if ("POISON_MESSAGE_TEST".equals(message.getClassName())) {
                log.error("DLQ TEST: Processing poison message with invalid ID: {}", message.getClassId());
                throw new IllegalArgumentException("Poison message detected - Invalid class ID: " + message.getClassId());
            }
            
            // Check for runtime exception trigger
            if ("THROW_RUNTIME_EXCEPTION".equals(message.getClassName())) {
                log.error("DLQ TEST: Throwing runtime exception as requested");
                throw new RuntimeException("Intentional runtime exception for DLQ testing");
            }
            
            // Check for batch failure trigger
            if (message.getPayload() != null && message.getPayload().toString().contains("BATCH_FAILURE_TRIGGER")) {
                log.error("DLQ TEST: Batch failure trigger detected for message: {}", message.getClassName());
                throw new IllegalStateException("Batch failure test - Message: " + message.getClassName());
            }
            
            // Check for consumer failure trigger
            if (message.getPayload() != null && message.getPayload().toString().contains("TRIGGER_CONSUMER_FAILURE")) {
                log.error("DLQ TEST: Consumer failure trigger detected");
                throw new RuntimeException("Consumer failure triggered by payload: " + message.getPayload());
            }
            
            // Process the message based on action type
            switch (message.getAction()) {
                case "CREATE":
                    handleClassCreated(message);
                    break;
                case "UPDATE":
                    handleClassUpdated(message);
                    break;
                case "DELETE":
                    handleClassDeleted(message);
                    break;
                default:
                    log.warn("Unknown action type: {}", message.getAction());
            }
            
            log.info("Successfully processed class message for class ID: {}", message.getClassId());
            
        } catch (Exception e) {
            log.error("Failed to process class message: {}, Error: {}", message, e.getMessage(), e);
            // In a real scenario, you might want to send to DLQ or retry
            throw new RuntimeException("Failed to process message", e);
        }
    }
    
    /**
     * Handles class creation messages
     */
    private void handleClassCreated(ClassMessage message) {
        log.info("Processing class creation for ID: {}, Name: {}", 
                message.getClassId(), message.getClassName());
        
        // Add your business logic here
        // For example: send notifications, update cache, trigger other services, etc.
        
        // Example: Log the creation
        log.info("Class '{}' with ID {} has been created successfully", 
                message.getClassName(), message.getClassId());
    }
    
    /**
     * Handles class update messages
     */
    private void handleClassUpdated(ClassMessage message) {
        log.info("Processing class update for ID: {}, Name: {}", 
                message.getClassId(), message.getClassName());
        
        // Add your business logic here
        // For example: invalidate cache, update search index, notify subscribers, etc.
        
        log.info("Class '{}' with ID {} has been updated successfully", 
                message.getClassName(), message.getClassId());
    }
    
    /**
     * Handles class deletion messages
     */
    private void handleClassDeleted(ClassMessage message) {
        log.info("Processing class deletion for ID: {}, Name: {}", 
                message.getClassId(), message.getClassName());
        
        // Add your business logic here
        // For example: cleanup related data, remove from cache, notify subscribers, etc.
        
        log.info("Class '{}' with ID {} has been deleted successfully", 
                message.getClassName(), message.getClassId());
    }
    
    /**
     * Handles messages from Dead Letter Queue
     */
    @RabbitListener(queues = "${app.rabbitmq.class.dead-letter.queue.name}")
    public void handleDeadLetterMessage(ClassMessage message) {
        log.error("Received message from Dead Letter Queue: {}", message);
        
        // Handle failed messages
        // For example: save to database for manual review, send alerts, etc.
        
        log.error("Message processing failed multiple times for class ID: {}", message.getClassId());
    }
}
