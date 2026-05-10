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

    private static final int LIMIT = 10;

    public List<CourseSuggestionResponse> execute(UUID userId, String keyword) {
        if (keyword == null || keyword.trim().isEmpty() || userId == null) {
            return List.of();
        }
        String q = keyword.trim();

        Map<UUID, CourseSuggestionResponse> merged = new LinkedHashMap<>();

        // Keyword first
        List<CourseSuggestionResponse> keywordResults = courseRepository.keywordSearchCourses(userId, q, LIMIT);
        for (CourseSuggestionResponse r : keywordResults) merged.put(r.courseId(), r);

        // Only embed + semantic search if keyword didn't fill the limit
        if (merged.size() < LIMIT) {
            float[] vector = embeddingService.embed(q);
            String pgVector = embeddingService.toPgVector(vector);
            List<CourseSuggestionResponse> semantic = courseRepository.semanticSearchCourses(userId, pgVector, LIMIT);
            for (CourseSuggestionResponse r : semantic) merged.putIfAbsent(r.courseId(), r);
        }

        List<CourseSuggestionResponse> result = new ArrayList<>(merged.values());
        return result.size() > LIMIT ? result.subList(0, LIMIT) : result;
    }
}