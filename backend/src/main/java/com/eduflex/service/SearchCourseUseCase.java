package com.eduflex.service;

import com.eduflex.dto.CourseSearchDTO.CourseSuggestionResponse;
import com.eduflex.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
public class SearchCourseUseCase {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EmbeddingService embeddingService;

    public List<CourseSuggestionResponse> execute(UUID userId, String keyword) {
        if (keyword == null || keyword.trim().isEmpty() || userId == null) {
            return List.of();
        }
        float[] vector = embeddingService.embed(keyword.trim());
        String pgVector = embeddingService.toPgVector(vector);
        return courseRepository.semanticSearchCourses(pgVector, 10);
    }
}