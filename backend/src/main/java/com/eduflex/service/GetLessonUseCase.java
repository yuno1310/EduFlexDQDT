package com.eduflex.service;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eduflex.dto.GetLessonDTO.GetLessonRequest;
import com.eduflex.dto.GetLessonDTO.GetLessonResponse;
import com.eduflex.dto.GetLessonDTO.LessonInfo;
import com.eduflex.repository.LessonRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class GetLessonUseCase {
  @Autowired
  private LessonRepository lessonRepository;

  public GetLessonResponse execute(GetLessonRequest request) {
    var lessons = lessonRepository.getLesson(request.courseID());
    if (lessons != null && lessons.isEmpty() != true) {
      var listLessons = new ArrayList<LessonInfo>();
      for (var record : lessons) {
        var lesson = new LessonInfo(record.title(), record.contentType());
        listLessons.add(lesson);
      }
      return new GetLessonResponse(true, "Loading list lessons successfuly", listLessons);
    } else {
      return new GetLessonResponse(false, "Failed to load list lesson", null);
    }
  }
}
