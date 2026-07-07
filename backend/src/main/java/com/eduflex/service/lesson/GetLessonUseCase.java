package com.eduflex.service.lesson;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.eduflex.dto.lesson.GetLessonDTO.GetLessonRequest;
import com.eduflex.dto.lesson.GetLessonDTO.GetLessonResponse;
import com.eduflex.dto.lesson.GetLessonDTO.LessonInfo;
import com.eduflex.repository.course.CourseRepository;
import com.eduflex.repository.lesson.LessonRepository;

@Service
public class GetLessonUseCase {
  @Autowired
  private LessonRepository lessonRepository;

  @Autowired
  private CourseRepository courseRepository;

  @Cacheable(value = "lessons", key = "#request.courseID().toString()")
  public GetLessonResponse execute(GetLessonRequest request) {
    if (request.courseID() == null) {
      return new GetLessonResponse(false, "CourseID null, failed to load list lesson", null);
    }
    if (!courseRepository.existsById(request.courseID())) {
      return new GetLessonResponse(false, "Course was not existed!", null);
    }
    var lessons = lessonRepository.getLesson(request.courseID());
    if (lessons == null || lessons.isEmpty()) {
      return new GetLessonResponse(true, "List null", null);
    }
    var listLessons = new ArrayList<LessonInfo>();
    for (var record : lessons) {
      var lesson = new LessonInfo(record.lessonID(), record.title(), record.contentType(),
          record.videoUrl(), record.content(), record.parentLessonId());
      listLessons.add(lesson);
    }
    return new GetLessonResponse(true, "Loading list lessons successfully", listLessons);
  }
}
