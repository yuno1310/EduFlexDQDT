package com.eduflex.service;

import com.eduflex.dto.CourseSearchDTO.CourseSuggestionResponse;
import com.eduflex.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
        String q = keyword.trim();

        float[] vector = embeddingService.embed(q);
        String pgVector = embeddingService.toPgVector(vector);

        List<CourseSuggestionResponse> semantic = courseRepository.semanticSearchCourses(userId, pgVector, 10);
        List<CourseSuggestionResponse> keywordResults = courseRepository.keywordSearchCourses(userId, q, 10);

        // Merge: semantic first, then keyword results not already included
        Map<UUID, CourseSuggestionResponse> merged = new LinkedHashMap<>();
        for (CourseSuggestionResponse r : keywordResults) merged.put(r.courseId(), r);
        for (CourseSuggestionResponse r : semantic) merged.putIfAbsent(r.courseId(), r);

        return new ArrayList<>(merged.values());
    }
}