package com.eduflex.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.eduflex.dto.AiCourseDTO.AskCourseResponse;
import com.eduflex.dto.AiCourseDTO.CourseMaterial;
import com.eduflex.dto.AiCourseDTO.CourseSummaryResponse;
import com.eduflex.dto.AiCourseDTO.LessonContext;
import com.eduflex.dto.AiCourseDTO.Source;
import com.eduflex.exception.ResourceNotFoundException;
import com.eduflex.repository.CourseRepository;
import com.fasterxml.jackson.databind.JsonNode;

@Service
public class AiCourseService {
  private static final int MAX_CONTEXT_CHARS = 14_000;

  private final CourseRepository courses;
  private final EmbeddingService embeddings;
  private final RestClient http;
  private final String apiKey;
  private final String model;
  private final String baseUrl;

  public AiCourseService(CourseRepository courses, EmbeddingService embeddings,
      RestClient.Builder restClientBuilder,
      @Value("${ai.gemini.api-key:}") String apiKey,
      @Value("${ai.gemini.model:gemini-2.0-flash}") String model,
      @Value("${ai.gemini.base-url}") String baseUrl) {
    this.courses = courses;
    this.embeddings = embeddings;
    this.http = restClientBuilder.build();
    this.apiKey = apiKey;
    this.model = model;
    this.baseUrl = baseUrl;
  }

  @Cacheable(value = "courseSummaries", key = "#courseId", unless = "!#result.generatedByAi()")
  public CourseSummaryResponse summarize(UUID courseId) {
    CourseMaterial course = requireCourse(courseId);
    List<LessonContext> lessons = courses.getCourseLessonContext(courseId);
    String context = buildContext(course, lessons);

    if (apiKey.isBlank()) {
      return new CourseSummaryResponse(courseId, fallbackSummary(course, lessons), false);
    }

    String prompt = """
        You are EduFlex's learning assistant. Summarize only the supplied course context.
        Explain what the learner will study, concrete skills they can gain, intended audience,
        and the learning formats. Use a warm, concise mobile-friendly style with 3-5 short
        paragraphs. Never invent topics that are absent from the context.

        COURSE CONTEXT:
        """ + context;
    try {
      return new CourseSummaryResponse(courseId, generate(prompt), true);
    } catch (RuntimeException providerError) {
      return new CourseSummaryResponse(courseId, fallbackSummary(course, lessons), false);
    }
  }

  public AskCourseResponse ask(UUID courseId, String question) {
    CourseMaterial course = requireCourse(courseId);
    String vector = embeddings.toPgVector(embeddings.embed(question));
    List<LessonContext> retrieved = courses.searchLessonContext(courseId, vector, 5);
    if (retrieved.isEmpty()) retrieved = courses.getCourseLessonContext(courseId).stream().limit(5).toList();

    List<Source> sources = retrieved.stream()
        .map(item -> new Source(item.lessonId(), item.title(), item.relevance()))
        .toList();

    if (apiKey.isBlank()) {
      String answer = "AI generation is not configured yet. The most relevant lessons are: "
          + retrieved.stream().map(LessonContext::title).reduce((a, b) -> a + ", " + b).orElse("none");
      return new AskCourseResponse(answer, sources, false);
    }

    String prompt = """
        Answer the learner's question using only the retrieved course excerpts below.
        If the excerpts do not contain the answer, say that clearly. Keep the answer concise,
        actionable, and cite lesson titles in parentheses.

        Course: %s
        Question: %s

        RETRIEVED EXCERPTS:
        %s
        """.formatted(course.title(), question, buildContext(course, retrieved));
    try {
      return new AskCourseResponse(generate(prompt), sources, true);
    } catch (RuntimeException providerError) {
      String answer = "The AI provider is temporarily unavailable. Review these relevant lessons: "
          + retrieved.stream().map(LessonContext::title).reduce((a, b) -> a + ", " + b).orElse("none");
      return new AskCourseResponse(answer, sources, false);
    }
  }

  private CourseMaterial requireCourse(UUID courseId) {
    CourseMaterial material = courses.getCourseMaterial(courseId);
    if (material == null) throw new ResourceNotFoundException("Course not found");
    return material;
  }

  private String buildContext(CourseMaterial course, List<LessonContext> lessons) {
    StringBuilder context = new StringBuilder()
        .append("Title: ").append(course.title()).append('\n')
        .append("Description: ").append(safe(course.description())).append('\n')
        .append("Learning model: ").append(safe(course.learningModel())).append("\n\n");
    for (LessonContext lesson : lessons) {
      String excerpt = safe(lesson.content());
      if (excerpt.length() > 1_500) excerpt = excerpt.substring(0, 1_500);
      if (context.length() + excerpt.length() > MAX_CONTEXT_CHARS) break;
      context.append("Lesson: ").append(lesson.title()).append('\n')
          .append(excerpt).append("\n\n");
    }
    return context.toString();
  }

  private String fallbackSummary(CourseMaterial course, List<LessonContext> lessons) {
    List<String> titles = new ArrayList<>();
    for (LessonContext lesson : lessons) {
      if (titles.size() == 5) break;
      titles.add(lesson.title());
    }
    String topics = titles.isEmpty() ? "a structured set of lessons" : String.join(", ", titles);
    return course.title() + " is a " + safe(course.learningModel())
        + " course covering " + topics + ".\n\n"
        + (safe(course.description()).isBlank() ? "Explore the lessons to build practical knowledge at your own pace."
            : course.description());
  }

  private String generate(String prompt) {
    Map<String, Object> body = Map.of(
        "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
        "generationConfig", Map.of("temperature", 0.2, "maxOutputTokens", 700));

    JsonNode response = http.post()
        .uri(baseUrl + "/v1beta/models/{model}:generateContent?key={key}", model, apiKey)
        .contentType(MediaType.APPLICATION_JSON)
        .body(body)
        .retrieve()
        .body(JsonNode.class);

    String text = response == null ? "" :
        response.path("candidates").path(0).path("content").path("parts").path(0).path("text").asText();
    if (text.isBlank()) throw new IllegalStateException("AI provider returned an empty response");
    return text;
  }

  private String safe(String value) {
    return value == null ? "" : value.trim();
  }
}
