package com.example.demo.service.messaging;

import com.example.demo.dto.message.ClassMessage;

/**
 * Interface for class messaging operations
 * Following Single Responsibility and Interface Segregation principles
 */
public interface ClassMessagingService {
    
    /**
     * Publishes a generic class message
     */
    void publishMessage(ClassMessage message);
    
    /**
     * Publishes a class creation notification
     */
    void notifyClassCreated(Integer classId, String className);
    
    /**
     * Publishes a class update notification
     */
    void notifyClassUpdated(Integer classId, String className);
    
    /**
     * Publishes a class deletion notification
     */
    void notifyClassDeleted(Integer classId, String className);
    
    /**
     * Publishes a class operation failure notification
     */
    void notifyClassOperationFailed(Integer classId, String className, String action, String errorMessage);
}
