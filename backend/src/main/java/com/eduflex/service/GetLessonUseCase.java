package com.eduflex.service;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eduflex.dto.GetLessonDTO.GetLessonRequest;
import com.eduflex.dto.GetLessonDTO.GetLessonResponse;
import com.eduflex.dto.GetLessonDTO.LessonInfo;
import com.eduflex.repository.CourseRepository;
import com.eduflex.repository.LessonRepository;

@Service
public class GetLessonUseCase {
  @Autowired
  private LessonRepository lessonRepository;

  @Autowired
  private CourseRepository courseRepository;

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
      var lesson = new LessonInfo(record.lessonID(), record.title(), record.contentType());
      listLessons.add(lesson);
    }
    return new GetLessonResponse(true, "Loading list lessons successfully", listLessons);
  }
}
