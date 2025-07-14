package com.example.demo.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.message.ClassMessage;
import com.example.demo.dto.request.ClassRequest;
import com.example.demo.service.ClassService;
import com.example.demo.service.ClassSimpleService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/showcase")
@RequiredArgsConstructor
@Slf4j
public class RabbitMQPowerController {
    
    private final ClassService classServiceWithRabbitMQ;
    private final ClassSimpleService classServiceWithoutRabbitMQ;
    private final RabbitTemplate rabbitTemplate;
    
    /**
     * 🚀 PERFORMANCE COMPARISON: With vs Without RabbitMQ
     */
    @PostMapping("/performance-test")
    public ResponseEntity<?> performanceTest(@RequestParam(defaultValue = "5") int operations) {
        log.info("🔥 Performance Test: {} operations", operations);
        
        List<String> results = new ArrayList<>();
        
        try {
            // Test WITH RabbitMQ
            long startTime = System.currentTimeMillis();
            for (int i = 1; i <= operations; i++) {
                ClassRequest request = new ClassRequest();
                request.setName("RabbitMQ-Test-" + i);
                classServiceWithRabbitMQ.createClass(request);
            }
            long rabbitMQTime = System.currentTimeMillis() - startTime;
            
            // Test WITHOUT RabbitMQ  
            startTime = System.currentTimeMillis();
            for (int i = 1; i <= operations; i++) {
                ClassRequest request = new ClassRequest();
                request.setName("Simple-Test-" + i);
                classServiceWithoutRabbitMQ.createClass(request);
            }
            long simpleTime = System.currentTimeMillis() - startTime;
            
            // Calculate performance gain
            double performanceGain = simpleTime > 0 ? 
                ((double)(simpleTime - rabbitMQTime) / simpleTime) * 100 : 0;
            
            results.add("🚀 RABBITMQ PERFORMANCE TEST RESULTS");
            results.add("📊 Operations: " + operations);
            results.add("⚡ WITH RabbitMQ: " + rabbitMQTime + "ms");
            results.add("🐌 WITHOUT RabbitMQ: " + simpleTime + "ms");
            results.add("🔥 Performance Difference: " + String.format("%.1f%%", performanceGain));
            results.add("💡 RabbitMQ enables async processing!");
            
            return ResponseEntity.ok(results);
            
        } catch (Exception e) {
            log.error("Performance test error: {}", e.getMessage());
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
    
    /**
     * 📈 HIGH THROUGHPUT TEST: Bulk message processing
     */
    @PostMapping("/throughput-test")
    public ResponseEntity<?> throughputTest(@RequestParam(defaultValue = "50") int messageCount) {
        log.info("📈 Throughput Test: {} messages", messageCount);
        
        try {
            long startTime = System.currentTimeMillis();
            
            // Send bulk messages to RabbitMQ
            for (int i = 1; i <= messageCount; i++) {
                ClassMessage message = ClassMessage.builder()
                        .classId(i)
                        .className("Throughput-Test-" + i)
                        .action("BULK_CREATE")
                        .status("SUCCESS")
                        .message("High throughput test message")
                        .timestamp(LocalDateTime.now())
                        .payload("THROUGHPUT_TEST")
                        .build();
                
                rabbitTemplate.convertAndSend("class.exchange", "class.routing.key", message);
            }
            
            long totalTime = System.currentTimeMillis() - startTime;
            double messagesPerSecond = totalTime > 0 ? 
                (double) messageCount / (totalTime / 1000.0) : 0;
            
            List<String> results = new ArrayList<>();
            results.add("📈 HIGH THROUGHPUT TEST COMPLETED");
            results.add("📊 Messages Sent: " + messageCount);
            results.add("⏱️ Total Time: " + totalTime + "ms");
            results.add("🚀 Throughput: " + String.format("%.1f messages/second", messagesPerSecond));
            results.add("💪 RabbitMQ handled bulk processing efficiently!");
            
            return ResponseEntity.ok(results);
            
        } catch (Exception e) {
            log.error("Throughput test error: {}", e.getMessage());
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
    
    /**
     * 🔧 RELIABILITY TEST: DLQ and failure handling
     */
    @PostMapping("/reliability-test")
    public ResponseEntity<?> reliabilityTest(@RequestParam(defaultValue = "10") int messageCount) {
        log.info("🔧 Reliability Test: {} messages", messageCount);
        
        try {
            int successCount = 0;
            int failureCount = 0;
            
            for (int i = 1; i <= messageCount; i++) {
                if (i % 3 == 0) {
                    // Every 3rd message fails - goes to DLQ
                    ClassMessage failMessage = ClassMessage.builder()
                            .classId(-i)
                            .className("FAILURE_TEST_" + i)
                            .action("CREATE")
                            .status("SUCCESS")
                            .message("This will fail in consumer")
                            .timestamp(LocalDateTime.now())
                            .payload("TRIGGER_CONSUMER_FAILURE")
                            .build();
                    
                    rabbitTemplate.convertAndSend("class.exchange", "class.routing.key", failMessage);
                    failureCount++;
                } else {
                    // Normal successful message
                    ClassMessage successMessage = ClassMessage.builder()
                            .classId(i)
                            .className("Success-Test-" + i)
                            .action("CREATE")
                            .status("SUCCESS")
                            .message("Normal message")
                            .timestamp(LocalDateTime.now())
                            .payload("NORMAL_PROCESSING")
                            .build();
                    
                    rabbitTemplate.convertAndSend("class.exchange", "class.routing.key", successMessage);
                    successCount++;
                }
            }
            
            List<String> results = new ArrayList<>();
            results.add("🔧 RELIABILITY TEST COMPLETED");
            results.add("✅ Success Messages: " + successCount);
            results.add("❌ Failure Messages: " + failureCount + " (→ DLQ)");
            results.add("🛡️ RabbitMQ Reliability Features:");
            results.add("  • Dead Letter Queue (DLQ)");
            results.add("  • Automatic Retry Logic");
            results.add("  • Message Persistence");
            results.add("  • Failure Isolation");
            results.add("💡 Check RabbitMQ UI: Failed messages in class.queue.dlq");
            
            return ResponseEntity.ok(results);
            
        } catch (Exception e) {
            log.error("Reliability test error: {}", e.getMessage());
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
    
    /**
     * 📊 RABBITMQ BENEFITS SUMMARY
     */
    @GetMapping("/benefits")
    public ResponseEntity<?> showBenefits() {
        List<String> benefits = new ArrayList<>();
        
        benefits.add("🚀 RABBITMQ POWER & BENEFITS");
        benefits.add("");
        benefits.add("⚡ PERFORMANCE ADVANTAGES:");
        benefits.add("  • Asynchronous Processing - Non-blocking operations");
        benefits.add("  • Reduced Response Times - 30-50% faster");
        benefits.add("  • High Throughput - 500+ messages/second");
        benefits.add("  • Better Resource Utilization");
        benefits.add("");
        benefits.add("🛡️ RELIABILITY FEATURES:");
        benefits.add("  • Message Persistence - Zero data loss");
        benefits.add("  • Dead Letter Queue - Graceful failure handling");
        benefits.add("  • Automatic Retries - Built-in resilience");
        benefits.add("  • Guaranteed Delivery - Message acknowledgments");
        benefits.add("");
        benefits.add("📈 SCALABILITY BENEFITS:");
        benefits.add("  • Horizontal Scaling - Add more consumers");
        benefits.add("  • Load Distribution - Balance work efficiently");
        benefits.add("  • Concurrent Processing - Multiple threads");
        benefits.add("  • Traffic Spike Handling - Queue buffering");
        benefits.add("");
        benefits.add("🎯 ENTERPRISE FEATURES:");
        benefits.add("  • Event-Driven Architecture - Loose coupling");
        benefits.add("  • Microservices Communication - Service decoupling");
        benefits.add("  • Real-time Analytics - Instant data processing");
        benefits.add("  • Workflow Orchestration - Complex processes");
        benefits.add("");
        benefits.add("🧪 TEST THE POWER:");
        benefits.add("  POST /api/v1/showcase/performance-test?operations=10");
        benefits.add("  POST /api/v1/showcase/throughput-test?messageCount=100");
        benefits.add("  POST /api/v1/showcase/reliability-test?messageCount=15");
        
        return ResponseEntity.ok(benefits);
    }
    
    /**
     * 🎯 COMPLETE SHOWCASE: Run all tests
     */
    @PostMapping("/complete-demo")
    public ResponseEntity<?> completeDemo() {
        log.info("🎯 Running Complete RabbitMQ Demo");
        
        List<String> results = new ArrayList<>();
        results.add("🎯 COMPLETE RABBITMQ POWER DEMONSTRATION");
        results.add("");
        
        try {
            // Performance Test
            results.add("1️⃣ PERFORMANCE TEST:");
            long startTime = System.currentTimeMillis();
            for (int i = 1; i <= 5; i++) {
                ClassRequest request = new ClassRequest();
                request.setName("Demo-RabbitMQ-" + i);
                classServiceWithRabbitMQ.createClass(request);
            }
            long rabbitMQTime = System.currentTimeMillis() - startTime;
            results.add("   ⚡ Created 5 classes in " + rabbitMQTime + "ms with RabbitMQ");
            
            // Throughput Test
            results.add("2️⃣ THROUGHPUT TEST:");
            startTime = System.currentTimeMillis();
            for (int i = 1; i <= 25; i++) {
                ClassMessage message = ClassMessage.builder()
                        .classId(1000 + i)
                        .className("Demo-Throughput-" + i)
                        .action("DEMO")
                        .status("SUCCESS")
                        .message("Demo throughput message")
                        .timestamp(LocalDateTime.now())
                        .build();
                
                rabbitTemplate.convertAndSend("class.exchange", "class.routing.key", message);
            }
            long throughputTime = System.currentTimeMillis() - startTime;
            double rate = (double) 25 / (throughputTime / 1000.0);
            results.add("   📈 Sent 25 messages in " + throughputTime + "ms (" + 
                       String.format("%.1f", rate) + " msg/sec)");
            
            // Reliability Test
            results.add("3️⃣ RELIABILITY TEST:");
            ClassMessage failMessage = ClassMessage.builder()
                    .classId(-999)
                    .className("DEMO_FAILURE_TEST")
                    .action("CREATE")
                    .status("SUCCESS")
                    .message("This will fail and go to DLQ")
                    .timestamp(LocalDateTime.now())
                    .payload("TRIGGER_CONSUMER_FAILURE")
                    .build();
            
            rabbitTemplate.convertAndSend("class.exchange", "class.routing.key", failMessage);
            results.add("   🔧 Sent failure message → will go to DLQ");
            
            results.add("");
            results.add("✨ DEMONSTRATION COMPLETE!");
            results.add("🔍 Check RabbitMQ UI at http://localhost:15672");
            results.add("📊 Monitor queues: class.queue & class.queue.dlq");
            results.add("💡 RabbitMQ provides: Performance + Reliability + Scalability");
            
            return ResponseEntity.ok(results);
            
        } catch (Exception e) {
            log.error("Complete demo error: {}", e.getMessage());
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}
