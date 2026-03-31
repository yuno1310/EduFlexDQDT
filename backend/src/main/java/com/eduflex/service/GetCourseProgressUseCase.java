package com.eduflex.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eduflex.dto.CourseProgressDTO.GetCourseProgressResponse;
import com.eduflex.dto.CourseProgressDTO.LessonProgressItem;
import com.eduflex.dto.CourseProgressDTO.LessonProgressInfo;
import com.eduflex.repository.CourseProgressRepository;

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
      return new GetCourseProgressResponse(true, "Chưa có bài học nào trong khóa này", lessonList);
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

    return new GetCourseProgressResponse(true, "Lấy lộ trình học thành công", lessonList);
  }
}
