package kore.notification.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Value("${app.rabbitmq.queue}")
    private String queueName;

    @Value("${app.rabbitmq.exchange}")
    private String exchangeName;

    @Value("${app.rabbitmq.dlx}")
    private String deadLetterExchangeName;

    @Value("${app.rabbitmq.dlq}")
    private String deadLetterQueueName;

    @Value("${app.rabbitmq.routing-key.appointment}")
    private String appointmentRoutingKey;

    @Value("${app.rabbitmq.routing-key.stock}")
    private String stockRoutingKey;

    @Value("${app.rabbitmq.routing-key.dead-letter}")
    private String deadLetterRoutingKey;

    @Bean
    Queue notificationQueue() {
        return QueueBuilder.durable(queueName)
                .deadLetterExchange(deadLetterExchangeName)
                .deadLetterRoutingKey(deadLetterRoutingKey)
                .build();
    }

    @Bean
    Queue notificationDeadLetterQueue() {
        return QueueBuilder.durable(deadLetterQueueName).build();
    }

    @Bean
    TopicExchange notificationExchange() {
        return new TopicExchange(exchangeName, true, false);
    }

    @Bean
    DirectExchange notificationDeadLetterExchange() {
        return new DirectExchange(deadLetterExchangeName, true, false);
    }

    @Bean
    Binding notificationBinding(
            @Qualifier("notificationQueue") Queue notificationQueue,
            TopicExchange notificationExchange) {
        return BindingBuilder.bind(notificationQueue).to(notificationExchange).with(appointmentRoutingKey);
    }

    @Bean
    Binding stockNotificationBinding(
            @Qualifier("notificationQueue") Queue notificationQueue,
            TopicExchange notificationExchange) {
        return BindingBuilder.bind(notificationQueue).to(notificationExchange).with(stockRoutingKey);
    }

    @Bean
    Binding deadLetterBinding(
            @Qualifier("notificationDeadLetterQueue") Queue notificationDeadLetterQueue,
            DirectExchange notificationDeadLetterExchange) {
        return BindingBuilder.bind(notificationDeadLetterQueue)
                .to(notificationDeadLetterExchange)
                .with(deadLetterRoutingKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
