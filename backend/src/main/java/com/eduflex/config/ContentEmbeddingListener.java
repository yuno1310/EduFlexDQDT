package com.eduflex.config;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import com.eduflex.repository.course.CourseRepository;
import com.eduflex.repository.lesson.LessonRepository;
import com.eduflex.service.media.EmbeddingService;
import com.eduflex.monitoring.EduFlexMetrics;

@Component
public class ContentEmbeddingListener {
  private final CourseRepository courses;
  private final LessonRepository lessons;
  private final EmbeddingService embeddings;
  private final CacheManager cacheManager;
  private final EduFlexMetrics metrics;

  public ContentEmbeddingListener(CourseRepository courses, LessonRepository lessons,
      EmbeddingService embeddings, CacheManager cacheManager, EduFlexMetrics metrics) {
    this.courses = courses;
    this.lessons = lessons;
    this.embeddings = embeddings;
    this.cacheManager = cacheManager;
    this.metrics = metrics;
  }

  @RabbitListener(queues = RabbitMQConfig.QUEUE_CONTENT_EMBEDDINGS)
  public void refreshEmbedding(ContentChangedEvent event) {
    metrics.timeEmbeddingProcessing(() -> {
      refreshEmbeddingInternal(event);
      return null;
    });
  }

  private void refreshEmbeddingInternal(ContentChangedEvent event) {
    if (event.type() == ContentChangedEvent.ContentType.COURSE) {
      String text = courses.getEmbeddingText(event.id());
      if (text != null) {
        courses.updateEmbedding(event.id(), embeddings.toPgVector(embeddings.embed(text)));
      }
    } else {
      String text = lessons.getEmbeddingText(event.id());
      if (text != null) {
        lessons.updateEmbedding(event.id(), embeddings.toPgVector(embeddings.embed(text)));
      }
    }

    var summaries = cacheManager.getCache("courseSummaries");
    if (summaries != null) summaries.clear();
    var search = cacheManager.getCache("semanticSearch");
    if (search != null) search.clear();
  }
}
