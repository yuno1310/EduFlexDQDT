package com.eduflex.service;

import com.eduflex.dto.AdminDTO.DeleteCourseResponse;
import com.eduflex.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class DeleteCourseUseCase {

    @Autowired
    private CourseRepository courseRepository;

    @Transactional
    public DeleteCourseResponse execute(UUID courseId) {
        if (!courseRepository.existsById(courseId)) {
            return new DeleteCourseResponse(false, "Course not found");
        }

        boolean deleted = courseRepository.deleteCourse(courseId);
        if (deleted) {
            return new DeleteCourseResponse(true, "Course deleted successfully");
        } else {
            return new DeleteCourseResponse(false, "Failed to delete course");
        }
    }
}
