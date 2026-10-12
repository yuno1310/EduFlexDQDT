package com.eduflex.security;

import com.eduflex.exception.ResourceNotFoundException;
import com.eduflex.repository.enrollment.EnrollmentRepository;
import com.eduflex.repository.lesson.LessonProgressRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CourseAccess {
  private final EnrollmentRepository enrollments;
  private final LessonProgressRepository lessons;

  public CourseAccess(EnrollmentRepository enrollments, LessonProgressRepository lessons) {
    this.enrollments = enrollments;
    this.lessons = lessons;
  }

  public void requireEnrollment(UUID userId, UUID courseId) {
    if (userId == null || courseId == null || !enrollments.isUserEnrolled(userId, courseId)) {
      throw new AccessDeniedException("Course enrollment is required");
    }
  }

  public void requireCourse(Authentication authentication, UUID courseId) {
    if (authentication != null && authentication.isAuthenticated()
        && authentication.getAuthorities().stream()
            .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()))) return;
    UUID userId = authentication != null && authentication.getPrincipal() instanceof UUID id ? id : null;
    requireEnrollment(userId, courseId);
  }

  public void requireLesson(Authentication authentication, UUID lessonId) {
    UUID courseId = lessons.getCourseIdByLessonId(lessonId);
    if (courseId == null) throw new ResourceNotFoundException("Lesson not found: " + lessonId);
    requireCourse(authentication, courseId);
  }
}
