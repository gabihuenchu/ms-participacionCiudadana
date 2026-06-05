package cl.catastrofescl.citizen.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitMQConfig {

    public static final String EXCHANGE = "catastrofescl.events";
    public static final String STOCK_CRITICAL_QUEUE = "stock.critical.queue";
    public static final String STOCK_CRITICAL_ROUTING_KEY = "stock.critical";

    @Bean
    public TopicExchange catastrofesclEventsExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue stockCriticalQueue() {
        return QueueBuilder.durable(STOCK_CRITICAL_QUEUE).build();
    }

    @Bean
    public Binding stockCriticalBinding(Queue stockCriticalQueue, TopicExchange catastrofesclEventsExchange) {
        return BindingBuilder
                .bind(stockCriticalQueue)
                .to(catastrofesclEventsExchange)
                .with(STOCK_CRITICAL_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        template.setMandatory(true);
        return template;
    }
}
