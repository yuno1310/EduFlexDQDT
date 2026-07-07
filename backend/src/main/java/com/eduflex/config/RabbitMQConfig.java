package com.eduflex.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE_GAMIFICATION = "gamification_queue";
    public static final String EXCHANGE_EDUFLEX = "eduflex_exchange";
    public static final String ROUTING_KEY_GAMIFICATION = "gamification_routing_key";

   
    @Bean
    public Queue gamificationQueue() {
        return new Queue(QUEUE_GAMIFICATION, true);
    }

  
    @Bean
    public DirectExchange eduflexExchange() {
        return new DirectExchange(EXCHANGE_EDUFLEX);
    }

   
    @Bean
    public Binding bindingGamification(Queue gamificationQueue, DirectExchange eduflexExchange) {
        return BindingBuilder.bind(gamificationQueue).to(eduflexExchange).with(ROUTING_KEY_GAMIFICATION);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
