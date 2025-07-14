package com.example.demo.dto.message;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassMessage {
    
    private Integer classId;
    private String className;
    private String action; // CREATE, UPDATE, DELETE
    private String status; // SUCCESS, FAILED
    private String message;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
    
    private Object payload; // Additional data if needed
    
    public static ClassMessage createMessage(Integer classId, String className, String action) {
        return ClassMessage.builder()
                .classId(classId)
                .className(className)
                .action(action)
                .status("PENDING")
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static ClassMessage successMessage(Integer classId, String className, String action, String message) {
        return ClassMessage.builder()
                .classId(classId)
                .className(className)
                .action(action)
                .status("SUCCESS")
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static ClassMessage failedMessage(Integer classId, String className, String action, String errorMessage) {
        return ClassMessage.builder()
                .classId(classId)
                .className(className)
                .action(action)
                .status("FAILED")
                .message(errorMessage)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
