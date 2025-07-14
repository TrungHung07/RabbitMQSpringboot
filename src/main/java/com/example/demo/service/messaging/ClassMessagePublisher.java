package com.example.demo.service.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.demo.dto.message.ClassMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClassMessagePublisher implements ClassMessagingService {
    
    private final RabbitTemplate rabbitTemplate;
    
    @Value("${app.rabbitmq.class.exchange.name}")
    private String classExchangeName;
    
    @Value("${app.rabbitmq.class.routing-key}")
    private String classRoutingKey;
    
    /**
     * Publishes a class message to RabbitMQ
     * 
     * @param message The class message to publish
     */
    @Override
    public void publishMessage(ClassMessage message) {
        try {
            log.info("Publishing class message: {}", message);
            rabbitTemplate.convertAndSend(classExchangeName, classRoutingKey, message);
            log.info("Successfully published class message for class ID: {}", message.getClassId());
        } catch (Exception e) {
            log.error("Failed to publish class message for class ID: {}, Error: {}", 
                     message.getClassId(), e.getMessage(), e);
            throw new RuntimeException("Failed to publish message", e);
        }
    }
    
    /**
     * Publishes a class creation message
     */
    @Override
    public void notifyClassCreated(Integer classId, String className) {
        ClassMessage message = ClassMessage.successMessage(classId, className, "CREATE", 
                                                           "Class created successfully");
        publishMessage(message);
    }
    
    /**
     * Publishes a class update message
     */
    @Override
    public void notifyClassUpdated(Integer classId, String className) {
        ClassMessage message = ClassMessage.successMessage(classId, className, "UPDATE", 
                                                           "Class updated successfully");
        publishMessage(message);
    }
    
    /**
     * Publishes a class deletion message
     */
    @Override
    public void notifyClassDeleted(Integer classId, String className) {
        ClassMessage message = ClassMessage.successMessage(classId, className, "DELETE", 
                                                           "Class deleted successfully");
        publishMessage(message);
    }
    
    /**
     * Publishes a class operation failure message
     */
    @Override
    public void notifyClassOperationFailed(Integer classId, String className, String action, String errorMessage) {
        ClassMessage message = ClassMessage.failedMessage(classId, className, action, errorMessage);
        publishMessage(message);
    }
}
