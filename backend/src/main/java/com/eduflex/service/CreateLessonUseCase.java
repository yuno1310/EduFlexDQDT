package com.eduflex.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eduflex.dto.CreateLessonDTO.CreateLessonRequest;
import com.eduflex.dto.CreateLessonDTO.CreateLessonResponse;
import com.eduflex.entity.LessonDbO;
import com.eduflex.repository.LessonRepository;

@Service
public class CreateLessonUseCase {
  @Autowired
  private LessonRepository lessonRepository;

  public CreateLessonResponse execute(CreateLessonRequest request) {
    var lesson = new LessonDbO(request.courseID(), request.title(), request.contentType());
    if (lessonRepository.save(lesson) == true) {
      return new CreateLessonResponse(true, "Create a new lesson sucessfully");
    } else {
      return new CreateLessonResponse(false, "Failed to create new lesson");
    }
  }
}
