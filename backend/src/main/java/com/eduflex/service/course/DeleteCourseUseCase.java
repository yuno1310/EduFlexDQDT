package com.eduflex.service.course;

import com.eduflex.dto.user.AdminDTO.DeleteCourseResponse;
import com.eduflex.repository.course.CourseRepository;
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
