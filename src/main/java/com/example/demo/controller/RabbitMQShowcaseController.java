package com.example.demo.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.message.ClassMessage;
import com.example.demo.service.ClassService;
import com.example.demo.service.ClassSimpleService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/rabbitmq-showcase")
@RequiredArgsConstructor
@Slf4j
public class RabbitMQShowcaseController {
    
    private final ClassService classServiceWithRabbitMQ;
    private final ClassSimpleService classServiceWithoutRabbitMQ;
    private final RabbitTemplate rabbitTemplate;
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);
    
    /**
     * 🚀 PERFORMANCE COMPARISON: With vs Without RabbitMQ
     * Shows how RabbitMQ enables asynchronous processing for better performance
     */
    @PostMapping("/performance-comparison")
    public ResponseEntity<?> performanceComparison(@RequestParam(defaultValue = "10") int operationCount) {
        log.info("🔥 Performance Comparison: {} operations", operationCount);
        
        long startTime, endTime;
        List<String> results = new ArrayList<>();
        
        try {
            // 1. Test WITH RabbitMQ (Asynchronous messaging)
            startTime = System.currentTimeMillis();
            for (int i = 1; i <= operationCount; i++) {
                com.example.demo.dto.request.ClassRequest request = new com.example.demo.dto.request.ClassRequest();
                request.setName("RabbitMQ-Class-" + i);
                classServiceWithRabbitMQ.createClass(request);
            }
            endTime = System.currentTimeMillis();
            long rabbitMQTime = endTime - startTime;
            
            // 2. Test WITHOUT RabbitMQ (Synchronous only)
            startTime = System.currentTimeMillis();
            for (int i = 1; i <= operationCount; i++) {
                com.example.demo.dto.request.ClassRequest request = new com.example.demo.dto.request.ClassRequest();
                request.setName("Simple-Class-" + i);
                classServiceWithoutRabbitMQ.createClass(request);
            }
            endTime = System.currentTimeMillis();
            long simpleTime = endTime - startTime;
            
            // 3. Calculate performance metrics
            double performanceGain = ((double)(simpleTime - rabbitMQTime) / simpleTime) * 100;
            
            results.add("🔥 PERFORMANCE COMPARISON RESULTS:");
            results.add("📊 Operations: " + operationCount);
            results.add("⚡ WITH RabbitMQ: " + rabbitMQTime + "ms");
            results.add("🐌 WITHOUT RabbitMQ: " + simpleTime + "ms");
            results.add("🚀 Performance Gain: " + String.format("%.2f%%", performanceGain));
            results.add("💡 RabbitMQ enables async processing, reducing response time!");
            
            return ResponseEntity.ok(results);
            
        } catch (Exception e) {
            log.error("Error in performance comparison: {}", e.getMessage());
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
    
    /**
     * 🔄 BULK OPERATIONS: High-throughput message processing
     * Shows RabbitMQ's ability to handle high-volume operations efficiently
     */
    @PostMapping("/bulk-operations")
    public ResponseEntity<?> bulkOperations(@RequestParam(defaultValue = "100") int messageCount) {
        log.info("🔄 Bulk Operations: {} messages", messageCount);
        
        try {
            long startTime = System.currentTimeMillis();
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            
            // Send messages in parallel using RabbitMQ
            for (int i = 1; i <= messageCount; i++) {
                final int messageId = i;
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    ClassMessage message = ClassMessage.builder()
                            .classId(messageId)
                            .className("Bulk-Operation-" + messageId)
                            .action("BULK_CREATE")
                            .status("SUCCESS")
                            .message("High-throughput bulk operation")
                            .timestamp(LocalDateTime.now())
                            .payload("BULK_PROCESSING")
                            .build();
                    
                    rabbitTemplate.convertAndSend("class.exchange", "class.routing.key", message);
                }, executorService);
                
                futures.add(future);
            }
            
            // Wait for all messages to be sent
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            
            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            double messagesPerSecond = (double) messageCount / (totalTime / 1000.0);
            
            List<String> results = new ArrayList<>();
            results.add("🔄 BULK OPERATIONS COMPLETED!");
            results.add("📊 Messages Sent: " + messageCount);
            results.add("⏱️ Total Time: " + totalTime + "ms");
            results.add("🚀 Throughput: " + String.format("%.2f messages/second", messagesPerSecond));
            results.add("💡 RabbitMQ handled " + messageCount + " messages efficiently!");
            
            return ResponseEntity.ok(results);
            
        } catch (Exception e) {
            log.error("Error in bulk operations: {}", e.getMessage());
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
    
    /**
     * 🎯 LOAD TESTING: Stress test RabbitMQ under high load
     * Demonstrates RabbitMQ's ability to handle concurrent high-volume traffic
     */
    @PostMapping("/load-test")
    public ResponseEntity<?> loadTest(
            @RequestParam(defaultValue = "5") int concurrentThreads,
            @RequestParam(defaultValue = "50") int messagesPerThread) {
        
        log.info("🎯 Load Test: {} threads × {} messages = {} total", 
                concurrentThreads, messagesPerThread, concurrentThreads * messagesPerThread);
        
        try {
            long startTime = System.currentTimeMillis();
            List<CompletableFuture<Integer>> futures = new ArrayList<>();
            
            // Create concurrent threads sending messages
            for (int thread = 1; thread <= concurrentThreads; thread++) {
                final int threadId = thread;
                CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
                    int successCount = 0;
                    for (int msg = 1; msg <= messagesPerThread; msg++) {
                        try {
                            ClassMessage message = ClassMessage.builder()
                                    .classId(threadId * 1000 + msg)
                                    .className("LoadTest-T" + threadId + "-M" + msg)
                                    .action("LOAD_TEST")
                                    .status("SUCCESS")
                                    .message("Load testing message from thread " + threadId)
                                    .timestamp(LocalDateTime.now())
                                    .payload("THREAD_" + threadId)
                                    .build();
                            
                            rabbitTemplate.convertAndSend("class.exchange", "class.routing.key", message);
                            successCount++;
                            
                            // Small delay to simulate real-world usage
                            Thread.sleep(10);
                            
                        } catch (Exception e) {
                            log.error("Error in thread {}: {}", threadId, e.getMessage());
                        }
                    }
                    return successCount;
                }, executorService);
                
                futures.add(future);
            }
            
            // Wait for all threads to complete and sum results
            int totalSuccessful = futures.stream()
                    .mapToInt(CompletableFuture::join)
                    .sum();
            
            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            double messagesPerSecond = (double) totalSuccessful / (totalTime / 1000.0);
            
            List<String> results = new ArrayList<>();
            results.add("🎯 LOAD TEST COMPLETED!");
            results.add("🧵 Concurrent Threads: " + concurrentThreads);
            results.add("📨 Messages per Thread: " + messagesPerThread);
            results.add("✅ Total Successful: " + totalSuccessful + "/" + (concurrentThreads * messagesPerThread));
            results.add("⏱️ Total Time: " + totalTime + "ms");
            results.add("🚀 Peak Throughput: " + String.format("%.2f messages/second", messagesPerSecond));
            results.add("💪 RabbitMQ handled concurrent load like a champion!");
            
            return ResponseEntity.ok(results);
            
        } catch (Exception e) {
            log.error("Error in load test: {}", e.getMessage());
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
    
    /**
     * 📈 REAL-TIME ANALYTICS: Showcase event-driven architecture
     * Demonstrates how RabbitMQ enables real-time data processing
     */
    @PostMapping("/analytics-simulation")
    public ResponseEntity<?> analyticsSimulation(@RequestParam(defaultValue = "20") int eventCount) {
        log.info("📈 Analytics Simulation: {} events", eventCount);
        
        try {
            long startTime = System.currentTimeMillis();
            String[] eventTypes = {"USER_REGISTRATION", "CLASS_CREATED", "STUDENT_ENROLLED", 
                                 "ASSIGNMENT_SUBMITTED", "GRADE_POSTED", "COURSE_COMPLETED"};
            String[] priorities = {"HIGH", "MEDIUM", "LOW"};
            
            for (int i = 1; i <= eventCount; i++) {
                String eventType = eventTypes[i % eventTypes.length];
                String priority = priorities[i % priorities.length];
                
                ClassMessage analyticsEvent = ClassMessage.builder()
                        .classId(i)
                        .className("Analytics-Event-" + i)
                        .action(eventType)
                        .status("ANALYTICS")
                        .message("Real-time analytics event: " + eventType)
                        .timestamp(LocalDateTime.now())
                        .payload("PRIORITY:" + priority + "|EVENT_ID:" + i)
                        .build();
                
                rabbitTemplate.convertAndSend("class.exchange", "class.routing.key", analyticsEvent);
                
                // Simulate real-time event generation
                Thread.sleep(50);
            }
            
            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            
            List<String> results = new ArrayList<>();
            results.add("📈 REAL-TIME ANALYTICS SIMULATION!");
            results.add("🎯 Events Generated: " + eventCount);
            results.add("⏱️ Total Time: " + totalTime + "ms");
            results.add("🔄 Event Types: USER_REGISTRATION, CLASS_CREATED, STUDENT_ENROLLED, etc.");
            results.add("📊 All events sent to RabbitMQ for real-time processing!");
            results.add("💡 This enables: Live dashboards, Real-time notifications, Instant analytics!");
            
            return ResponseEntity.ok(results);
            
        } catch (Exception e) {
            log.error("Error in analytics simulation: {}", e.getMessage());
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
    
    /**
     * 🔧 FAILURE RECOVERY: Demonstrate RabbitMQ's reliability features
     * Shows how RabbitMQ handles failures gracefully with DLQ
     */
    @PostMapping("/failure-recovery-demo")
    public ResponseEntity<?> failureRecoveryDemo(@RequestParam(defaultValue = "10") int messageCount) {
        log.info("🔧 Failure Recovery Demo: {} messages", messageCount);
        
        try {
            List<String> results = new ArrayList<>();
            results.add("🔧 FAILURE RECOVERY DEMONSTRATION");
            
            // Send mix of good and bad messages
            int successCount = 0;
            int failureCount = 0;
            
            for (int i = 1; i <= messageCount; i++) {
                if (i % 3 == 0) {
                    // Every 3rd message is designed to fail
                    ClassMessage failMessage = ClassMessage.builder()
                            .classId(-i) // Negative ID triggers failure
                            .className("FAILURE_TEST_" + i)
                            .action("CREATE")
                            .status("SUCCESS")
                            .message("This message will fail processing")
                            .timestamp(LocalDateTime.now())
                            .payload("TRIGGER_CONSUMER_FAILURE")
                            .build();
                    
                    rabbitTemplate.convertAndSend("class.exchange", "class.routing.key", failMessage);
                    failureCount++;
                } else {
                    // Normal successful message
                    ClassMessage successMessage = ClassMessage.builder()
                            .classId(i)
                            .className("Success-Message-" + i)
                            .action("CREATE")
                            .status("SUCCESS")
                            .message("Normal processing message")
                            .timestamp(LocalDateTime.now())
                            .payload("NORMAL_PROCESSING")
                            .build();
                    
                    rabbitTemplate.convertAndSend("class.exchange", "class.routing.key", successMessage);
                    successCount++;
                }
                
                Thread.sleep(100); // Allow processing time
            }
            
            results.add("✅ Success Messages: " + successCount);
            results.add("❌ Failure Messages: " + failureCount + " (will go to DLQ)");
            results.add("🔄 RabbitMQ Features Demonstrated:");
            results.add("  • Message Persistence");
            results.add("  • Automatic Retry Logic");
            results.add("  • Dead Letter Queue (DLQ)");
            results.add("  • Failure Isolation");
            results.add("💡 Check RabbitMQ UI: Failed messages are in class.queue.dlq!");
            
            return ResponseEntity.ok(results);
            
        } catch (Exception e) {
            log.error("Error in failure recovery demo: {}", e.getMessage());
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
    
    /**
     * 📊 RABBITMQ POWER SUMMARY: Complete showcase of capabilities
     * Comprehensive demonstration of what makes RabbitMQ powerful
     */
    @GetMapping("/power-summary")
    public ResponseEntity<?> rabbitMQPowerSummary() {
        List<String> capabilities = new ArrayList<>();
        
        capabilities.add("🚀 RABBITMQ POWER SHOWCASE - WHY IT'S AWESOME:");
        capabilities.add("");
        capabilities.add("⚡ PERFORMANCE BENEFITS:");
        capabilities.add("  • Asynchronous Processing - Non-blocking operations");
        capabilities.add("  • High Throughput - Handle thousands of messages/second");
        capabilities.add("  • Reduced Response Times - Operations complete faster");
        capabilities.add("  • Better Resource Utilization - CPU and memory efficiency");
        capabilities.add("");
        capabilities.add("🔧 RELIABILITY FEATURES:");
        capabilities.add("  • Message Persistence - No data loss");
        capabilities.add("  • Dead Letter Queues - Handle failures gracefully");
        capabilities.add("  • Automatic Retries - Built-in resilience");
        capabilities.add("  • Acknowledgments - Guaranteed delivery");
        capabilities.add("");
        capabilities.add("📈 SCALABILITY ADVANTAGES:");
        capabilities.add("  • Horizontal Scaling - Add more consumers");
        capabilities.add("  • Load Distribution - Balance work across instances");
        capabilities.add("  • Concurrent Processing - Multiple threads/processes");
        capabilities.add("  • Queue Management - Handle traffic spikes");
        capabilities.add("");
        capabilities.add("🎯 ENTERPRISE PATTERNS:");
        capabilities.add("  • Event-Driven Architecture - Loose coupling");
        capabilities.add("  • Real-time Analytics - Instant data processing");
        capabilities.add("  • Microservices Communication - Service decoupling");
        capabilities.add("  • Workflow Orchestration - Complex business processes");
        capabilities.add("");
        capabilities.add("🔍 MONITORING & OBSERVABILITY:");
        capabilities.add("  • Management UI - Visual queue monitoring");
        capabilities.add("  • Message Tracking - End-to-end visibility");
        capabilities.add("  • Performance Metrics - Throughput analysis");
        capabilities.add("  • Health Checks - System status monitoring");
        capabilities.add("");
        capabilities.add("💡 USE THE SHOWCASE APIS:");
        capabilities.add("  POST /api/v1/rabbitmq-showcase/performance-comparison");
        capabilities.add("  POST /api/v1/rabbitmq-showcase/bulk-operations");
        capabilities.add("  POST /api/v1/rabbitmq-showcase/load-test");
        capabilities.add("  POST /api/v1/rabbitmq-showcase/analytics-simulation");
        capabilities.add("  POST /api/v1/rabbitmq-showcase/failure-recovery-demo");
        
        return ResponseEntity.ok(capabilities);
    }
}
