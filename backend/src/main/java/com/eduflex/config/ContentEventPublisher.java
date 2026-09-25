package com.eduflex.config;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import com.eduflex.monitoring.EduFlexMetrics;

@Component
public class ContentEventPublisher {
  private static final Logger log = LoggerFactory.getLogger(ContentEventPublisher.class);

  private final RabbitTemplate rabbitTemplate;
  private final EduFlexMetrics metrics;

  public ContentEventPublisher(RabbitTemplate rabbitTemplate, EduFlexMetrics metrics) {
    this.rabbitTemplate = rabbitTemplate;
    this.metrics = metrics;
  }

  public void publish(ContentChangedEvent.ContentType type, UUID id) {
    ContentChangedEvent event = new ContentChangedEvent(type, id);
    if (TransactionSynchronizationManager.isActualTransactionActive()) {
      TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
        @Override
        public void afterCommit() {
          send(event);
        }
      });
      return;
    }
    send(event);
  }

  private void send(ContentChangedEvent event) {
    try {
      rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_EDUFLEX,
          RabbitMQConfig.ROUTING_KEY_CONTENT, event);
      metrics.embeddingPublish(true);
    } catch (AmqpException exception) {
      metrics.embeddingPublish(false);
      // Content writes remain available if the broker is temporarily down.
      log.warn("Could not publish embedding refresh for {} {}", event.type(), event.id(), exception);
    }
  }
}
