package com.example.demo.configuration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.rabbitmq.class")
public class RabbitMQProperties {
    
    private Queue queue = new Queue();
    private Exchange exchange = new Exchange();
    private String routingKey;
    private DeadLetter deadLetter = new DeadLetter();
    
    @Data
    public static class Queue {
        private String name;
    }
    
    @Data
    public static class Exchange {
        private String name;
    }
    
    @Data
    public static class DeadLetter {
        private Queue queue = new Queue();
        private Exchange exchange = new Exchange();
        
        @Data
        public static class Queue {
            private String name;
        }
        
        @Data
        public static class Exchange {
            private String name;
        }
    }
}
