package com.eduflex.service;

import com.eduflex.dto.AdminDTO.UpdateLessonRequest;
import com.eduflex.dto.AdminDTO.UpdateLessonResponse;
import com.eduflex.repository.LessonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UpdateLessonUseCase {

    @Autowired
    private LessonRepository lessonRepository;

    @Transactional
    public UpdateLessonResponse execute(UUID lessonId, UpdateLessonRequest request) {
        if (!lessonRepository.existsById(lessonId)) {
            return new UpdateLessonResponse(false, "Lesson not found");
        }

        boolean updated = lessonRepository.updateLesson(
                lessonId,
                request.title(),
                request.contentType(),
                request.videoUrl(),
                request.content(),
                request.parentLessonId()
        );

        return updated
                ? new UpdateLessonResponse(true, "Lesson updated successfully")
                : new UpdateLessonResponse(false, "Failed to update lesson");
    }
}
