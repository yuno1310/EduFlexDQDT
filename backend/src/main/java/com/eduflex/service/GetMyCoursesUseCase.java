package com.eduflex.service;

import com.eduflex.dto.CourseSearchDTO.CourseSuggestionResponse;
import com.eduflex.repository.CourseRepository;
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