package com.eduflex.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import com.eduflex.dto.AiCourseDTO.CourseMaterial;
import com.eduflex.dto.AiCourseDTO.LessonContext;
import com.eduflex.repository.course.CourseRepository;
import com.eduflex.service.media.EmbeddingService;
import com.eduflex.monitoring.EduFlexMetrics;

class AiCourseServiceTest {

  @Test
  void summaryRemainsUsefulWhenAiProviderIsNotConfigured() {
    CourseRepository courses = mock(CourseRepository.class);
    EmbeddingService embeddings = mock(EmbeddingService.class);
    EduFlexMetrics metrics = mock(EduFlexMetrics.class);
    UUID courseId = UUID.randomUUID();
    when(courses.getCourseMaterial(courseId))
        .thenReturn(new CourseMaterial(courseId, "Spring Boot", "Build secure APIs", "self-paced"));
    when(courses.getCourseLessonContext(courseId))
        .thenReturn(List.of(new LessonContext(UUID.randomUUID(), "Dependency injection", "IoC basics", 1.0)));

    AiCourseService service = new AiCourseService(
        courses, embeddings, RestClient.builder(), "", "unused", "https://example.invalid", metrics);

    var result = service.summarize(courseId);

    assertFalse(result.generatedByAi());
    assertTrue(result.summary().contains("Spring Boot"));
    assertTrue(result.summary().contains("Dependency injection"));
    verify(metrics).aiRequest("summary", false);
  }
}
