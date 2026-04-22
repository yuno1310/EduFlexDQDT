package com.eduflex.service;

import com.eduflex.dto.CourseSearchDTO.CourseSuggestionResponse;
import com.eduflex.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchCourseUseCase {

  @Autowired
  private CourseRepository courseRepository;

  public List<CourseSuggestionResponse> execute(String keyword) {
    if (keyword == null || keyword.trim().isEmpty()) {
      return List.of();
    }
    return courseRepository.searchCoursesByKeyword(keyword.trim(), 5);
  }
}
