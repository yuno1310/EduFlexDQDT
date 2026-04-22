package com.eduflex.service;

import com.eduflex.dto.AdminDTO.UpdateCourseRequest;
import com.eduflex.dto.AdminDTO.UpdateCourseResponse;
import com.eduflex.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UpdateCourseUseCase {

    @Autowired
    private CourseRepository courseRepository;

    @Transactional
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

        return updated
                ? new UpdateCourseResponse(true, "Course updated successfully")
                : new UpdateCourseResponse(false, "Failed to update course");
    }
}
