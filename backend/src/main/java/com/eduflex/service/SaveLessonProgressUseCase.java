package com.eduflex.service;

import com.eduflex.dto.ProgressDTO.SaveLessonRequest;
import com.eduflex.dto.ProgressDTO.SaveLessonResponse;
import com.eduflex.repository.LessonProgressRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class SaveLessonProgressUseCase {

  @Autowired
  private LessonProgressRepository progressRepository;

  @Transactional
  public SaveLessonResponse execute(SaveLessonRequest request) {
    try {
      UUID userId = request.userId();
      UUID lessonId = request.lessonId();
      progressRepository.upsertLessonProgress(userId, lessonId);
      UUID courseId = progressRepository.getCourseIdByLessonId(lessonId);
      if (courseId == null) {
        return new SaveLessonResponse(false, "Không tìm thấy khóa học chứa bài này", 0.0);
      }
      int totalLessons = progressRepository.countTotalLessonsInCourse(courseId);
      int completedLessons = progressRepository.countCompletedLessons(userId, courseId);
      double percent = totalLessons == 0 ? 0.0 : ((double) completedLessons / totalLessons) * 100;
      percent = Math.round(percent * 10.0) / 10.0; // Làm tròn 1 chữ số thập phân
      progressRepository.updateCourseProgress(userId, courseId, percent);
      return new SaveLessonResponse(true, "Lưu tiến độ thành công!", percent);

    } catch (Exception e) {
      e.printStackTrace();
      return new SaveLessonResponse(false, "Lỗi server: " + e.getMessage(), 0.0);
    }
  }
}
