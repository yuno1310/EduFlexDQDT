package com.eduflex.service.course;

import com.eduflex.dto.course.CourseSearchDTO.CourseSuggestionResponse;
import com.eduflex.repository.course.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
public class GetMyCoursesUseCase {

    @Autowired
    private CourseRepository courseRepository;

    public List<CourseSuggestionResponse> execute(UUID userId) {
        if (userId == null) {
            return List.of();
        }
        return courseRepository.getMyCourses(userId);
    }
}