package com.eduflex.service.lesson;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eduflex.dto.lesson.CourseProgressDTO.GetCourseProgressResponse;
import com.eduflex.dto.lesson.CourseProgressDTO.LessonProgressItem;
import com.eduflex.dto.lesson.CourseProgressDTO.LessonProgressInfo;
import com.eduflex.repository.gamification.CourseProgressRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class GetCourseProgressUseCase {
  @Autowired
  private CourseProgressRepository repository;

  public GetCourseProgressResponse execute(UUID userId, UUID courseId) {
    List<LessonProgressInfo> records = repository.getLessonsWithProgress(userId, courseId);
    List<LessonProgressItem> lessonList = new ArrayList<>();

    if (records == null) {
      return new GetCourseProgressResponse(true, "There are no lessons in this course yet", lessonList);
    }

    boolean previousCompleted = true;

    for (LessonProgressInfo record : records) {
      UUID lessonId = record.lessonId();
      String title = record.title();
      String contentType = record.contentType();
      Boolean isCompletedRaw = record.isCompleted();
      boolean isCompleted = (isCompletedRaw != null && isCompletedRaw);

      boolean isLocked = !previousCompleted;

      lessonList.add(new LessonProgressItem(lessonId, title, contentType, isCompleted, isLocked));

      previousCompleted = isCompleted;
    }

    return new GetCourseProgressResponse(true, "Successfully retrieved course progress", lessonList);
  }
}
