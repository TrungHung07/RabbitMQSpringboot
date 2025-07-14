package com.example.demo.configuration;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Queue Names
    @Value("${app.rabbitmq.class.queue.name}")
    private String classQueueName;

    @Value("${app.rabbitmq.class.dead-letter.queue.name}")
    private String classDeadLetterQueueName;

    // Exchange Names
    @Value("${app.rabbitmq.class.exchange.name}")
    private String classExchangeName;

    @Value("${app.rabbitmq.class.dead-letter.exchange.name}")
    private String classDeadLetterExchangeName;

    // Routing Keys
    @Value("${app.rabbitmq.class.routing-key}")
    private String classRoutingKey;

    // Message Converter
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // RabbitTemplate Configuration
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        template.setMandatory(true);
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                System.out.println("Message sent successfully");
            } else {
                System.err.println("Failed to send message: " + cause);
            }
        });
        template.setReturnsCallback(returned -> {
            System.err.println("Message returned: " + returned.getMessage());
        });
        return template;
    }

    // Listener Container Factory
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setConcurrentConsumers(3);
        factory.setMaxConcurrentConsumers(10);
        factory.setDefaultRequeueRejected(false);
        return factory;
    }

    // Dead Letter Exchange
    @Bean
    public DirectExchange classDeadLetterExchange() {
        return new DirectExchange(classDeadLetterExchangeName, true, false);
    }

    // Dead Letter Queue
    @Bean
    public Queue classDeadLetterQueue() {
        return QueueBuilder.durable(classDeadLetterQueueName).build();
    }

    // Main Direct Exchange
    @Bean
    public DirectExchange classExchange() {
        return new DirectExchange(classExchangeName, true, false);
    }

    // Main Queue with Dead Letter Configuration
    @Bean
    public Queue classQueue() {
        return QueueBuilder.durable(classQueueName)
                .withArgument("x-dead-letter-exchange", classDeadLetterExchangeName)
                .withArgument("x-dead-letter-routing-key", classDeadLetterQueueName)
                .withArgument("x-message-ttl", 300000) // 5 minutes TTL
                .build();
    }

    // Binding for Main Queue
    @Bean
    public Binding classBinding() {
        return BindingBuilder
                .bind(classQueue())
                .to(classExchange())
                .with(classRoutingKey);
    }

    // Binding for Dead Letter Queue
    @Bean
    public Binding classDeadLetterBinding() {
        return BindingBuilder
                .bind(classDeadLetterQueue())
                .to(classDeadLetterExchange())
                .with(classDeadLetterQueueName);
    }
}
