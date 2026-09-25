package com.eduflex.monitoring;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import io.micrometer.core.instrument.MeterRegistry;

/** Operational metrics with bounded labels only. */
@Component
public class EduFlexMetrics {
  private final MeterRegistry registry;

  public EduFlexMetrics(MeterRegistry registry) {
    this.registry = registry;

    // Initialize bounded counters so alert rules can distinguish zero from missing data.
    for (String outcome : new String[] {"new", "already_completed"}) {
      registry.counter("eduflex.progress.completions", "outcome", outcome);
    }
    for (String outcome : new String[] {"sent", "failed"}) {
      registry.counter("eduflex.embedding.publish", "outcome", outcome);
    }
    for (String outcome : new String[] {"success", "failed"}) {
      registry.timer("eduflex.embedding.processing", "outcome", outcome);
    }
    for (String source : new String[] {"lesson", "quiz", "course", "checkin", "quest"}) {
      registry.counter("eduflex.reward.events", "source", source);
    }
    for (String mode : new String[] {"summary", "ask"}) {
      for (String outcome : new String[] {"provider", "fallback"}) {
        registry.counter("eduflex.ai.requests", "mode", mode, "outcome", outcome);
      }
    }
  }

  public <T> T timeStatsLock(Supplier<T> operation) {
    return registry.timer("eduflex.stats.lock.acquisition").record(operation);
  }

  public void progressCompletion(boolean newlyCompleted) {
    afterCommit(() -> registry.counter("eduflex.progress.completions", "outcome",
        newlyCompleted ? "new" : "already_completed").increment());
  }

  public void reward(String source) {
    afterCommit(() -> registry.counter("eduflex.reward.events", "source", source).increment());
  }

  public void embeddingPublish(boolean succeeded) {
    registry.counter("eduflex.embedding.publish", "outcome", succeeded ? "sent" : "failed")
        .increment();
  }

  public void aiRequest(String mode, boolean generatedByProvider) {
    registry.counter("eduflex.ai.requests", "mode", mode, "outcome",
        generatedByProvider ? "provider" : "fallback").increment();
  }

  public <T> T timeEmbeddingProcessing(Supplier<T> operation) {
    long started = System.nanoTime();
    try {
      T result = operation.get();
      registry.timer("eduflex.embedding.processing", "outcome", "success")
          .record(System.nanoTime() - started, TimeUnit.NANOSECONDS);
      return result;
    } catch (RuntimeException exception) {
      registry.timer("eduflex.embedding.processing", "outcome", "failed")
          .record(System.nanoTime() - started, TimeUnit.NANOSECONDS);
      throw exception;
    }
  }

  private void afterCommit(Runnable action) {
    if (!TransactionSynchronizationManager.isActualTransactionActive()) {
      action.run();
      return;
    }
    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
      @Override
      public void afterCommit() {
        action.run();
      }
    });
  }
}
