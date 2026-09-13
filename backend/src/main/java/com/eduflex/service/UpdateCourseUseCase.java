package com.eduflex.service;

import com.eduflex.dto.AdminDTO.UpdateCourseRequest;
import com.eduflex.dto.AdminDTO.UpdateCourseResponse;
import com.eduflex.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.CacheEvict;
import com.eduflex.RabbitMQ.ContentChangedEvent;
import com.eduflex.RabbitMQ.ContentEventPublisher;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UpdateCourseUseCase {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ContentEventPublisher contentEvents;

    @Transactional
    @CacheEvict(value = {"courseCatalog", "courseSummaries", "semanticSearch"}, allEntries = true)
    public UpdateCourseResponse execute(UUID courseId, UpdateCourseRequest request) {
        if (!courseRepository.existsById(courseId)) {
            return new UpdateCourseResponse(false, "Course not found");
        }

        boolean updated = courseRepository.updateCourse(
                courseId,
                request.title(),
                request.learningModel(),
                request.status(),
                request.imageUrl(),
                request.price(),
                request.description()
        );

        if (updated) contentEvents.publish(ContentChangedEvent.ContentType.COURSE, courseId);

        return updated
                ? new UpdateCourseResponse(true, "Course updated successfully")
                : new UpdateCourseResponse(false, "Failed to update course");
    }
}
