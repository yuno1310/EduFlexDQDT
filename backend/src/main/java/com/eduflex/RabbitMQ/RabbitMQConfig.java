package com.eduflex.RabbitMQ;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Qualifier;

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE_GAMIFICATION = "gamification_queue";
    public static final String EXCHANGE_EDUFLEX = "eduflex_exchange";
    public static final String ROUTING_KEY_GAMIFICATION = "gamification_routing_key";
    public static final String QUEUE_CONTENT_EMBEDDINGS = "content_embeddings_queue";
    public static final String ROUTING_KEY_CONTENT = "content.changed";

   
    @Bean
    public Queue gamificationQueue() {
        return new Queue(QUEUE_GAMIFICATION, true);
    }

    @Bean
    public Queue contentEmbeddingsQueue() {
        return QueueBuilder.durable(QUEUE_CONTENT_EMBEDDINGS).build();
    }

  
    @Bean
    public DirectExchange eduflexExchange() {
        return new DirectExchange(EXCHANGE_EDUFLEX);
    }

   
    @Bean
    public Binding bindingGamification(@Qualifier("gamificationQueue") Queue gamificationQueue, DirectExchange eduflexExchange) {
        return BindingBuilder.bind(gamificationQueue).to(eduflexExchange).with(ROUTING_KEY_GAMIFICATION);
    }

    @Bean
    public Binding bindingContentEmbeddings(@Qualifier("contentEmbeddingsQueue") Queue contentEmbeddingsQueue, DirectExchange eduflexExchange) {
        return BindingBuilder.bind(contentEmbeddingsQueue).to(eduflexExchange).with(ROUTING_KEY_CONTENT);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
