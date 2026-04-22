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
        
        if (request.courseID() == null) {
            return new CreateLessonResponse(false, "Course ID is required!");
        }
        if (request.title() == null || request.title().trim().isEmpty()) {
            return new CreateLessonResponse(false, "Lesson title is required!");
        }

        var lesson = new LessonDbO(
            request.courseID(), 
            request.title(), 
            request.contentType(), 
            request.content()
        );
        if (lessonRepository.save(lesson)) { 
            return new CreateLessonResponse(true, "Lesson created successfully!");
        } else {
            return new CreateLessonResponse(false, "Failed to create new lesson.");
        }
    }
}