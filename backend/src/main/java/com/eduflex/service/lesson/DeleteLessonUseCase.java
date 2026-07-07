package com.eduflex.service.lesson;

import com.eduflex.dto.user.AdminDTO.DeleteLessonResponse;
import com.eduflex.repository.lesson.LessonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DeleteLessonUseCase {

    @Autowired
    private LessonRepository lessonRepository;

    public DeleteLessonResponse execute(UUID lessonId) {
        if (!lessonRepository.existsById(lessonId)) {
            return new DeleteLessonResponse(false, "Lesson not found");
        }

        boolean success = lessonRepository.deleteById(lessonId);
        if (success) {
            return new DeleteLessonResponse(true, "Lesson deleted successfully");
        } else {
            return new DeleteLessonResponse(false, "Failed to delete lesson");
        }
    }
}
