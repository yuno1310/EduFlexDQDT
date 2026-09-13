package com.eduflex.service;

import com.eduflex.dto.AdminDTO.UpdateLessonRequest;
import com.eduflex.dto.AdminDTO.UpdateLessonResponse;
import com.eduflex.repository.LessonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import com.eduflex.RabbitMQ.ContentChangedEvent;
import com.eduflex.RabbitMQ.ContentEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@CacheEvict(value = {"lessons", "courseSummaries", "semanticSearch"}, allEntries = true)
public class UpdateLessonUseCase {

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private ContentEventPublisher contentEvents;

    @Transactional
    public UpdateLessonResponse execute(UUID lessonId, UpdateLessonRequest request) {
        if (!lessonRepository.existsById(lessonId)) {
            return new UpdateLessonResponse(false, "Lesson not found");
        }

        UUID courseId = lessonRepository.findCourseId(lessonId);
        boolean updated = lessonRepository.updateLesson(
                lessonId,
                request.title(),
                request.contentType(),
                request.videoUrl(),
                request.content(),
                request.parentLessonId()
        );

        if (updated) {
            contentEvents.publish(ContentChangedEvent.ContentType.LESSON, lessonId);
            if (courseId != null) contentEvents.publish(ContentChangedEvent.ContentType.COURSE, courseId);
        }

        return updated
                ? new UpdateLessonResponse(true, "Lesson updated successfully")
                : new UpdateLessonResponse(false, "Failed to update lesson");
    }
}
