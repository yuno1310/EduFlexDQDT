package com.eduflex.service.lesson;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import com.eduflex.dto.lesson.CreateLessonDTO.CreateLessonRequest;
import com.eduflex.dto.lesson.CreateLessonDTO.CreateLessonResponse;
import com.eduflex.entity.lesson.LessonDbO;
import com.eduflex.repository.lesson.LessonRepository;

@Service
public class CreateLessonUseCase {

    @Autowired
    private LessonRepository lessonRepository;

    @Transactional
    public CreateLessonResponse execute(CreateLessonRequest request) {

        if (request.courseID() == null) {
            return new CreateLessonResponse(false, "Course ID is required!", null);
        }
        if (request.title() == null || request.title().trim().isEmpty()) {
            return new CreateLessonResponse(false, "Lesson title is required!", null);
        }

        try {
            // 1. Create the main lesson (TEXT/VIDEO)
            var lesson = new LessonDbO(
                request.courseID(),
                request.title(),
                request.contentType(),
                request.videoUrl(),
                request.content(),
                null // no parent for main lesson
            );
            UUID lessonId = lessonRepository.saveAndGetId(lesson);

            // 2. Auto-create an accompanying quiz lesson
            var quizLesson = new LessonDbO(
                request.courseID(),
                "Quiz: " + request.title(),
                "quiz",
                null, // no video url
                null, // no content
                lessonId // parent is the main lesson
            );
            lessonRepository.save(quizLesson);

            return new CreateLessonResponse(true, "Lesson created successfully!", lessonId);
        } catch (Exception e) {
            return new CreateLessonResponse(false, "Failed to create lesson: " + e.getMessage(), null);
        }
    }
}