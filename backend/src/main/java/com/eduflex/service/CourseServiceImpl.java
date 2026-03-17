package com.eduflex.service;

import com.eduflex.dto.CourseDTO;
import com.eduflex.entity.Course;
import com.eduflex.exception.ResourceNotFoundException;
import com.eduflex.repository.CourseRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;

    // Constructor injection — Spring auto-wires the repository bean
    public CourseServiceImpl(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Override
    public List<CourseDTO> getAllCourses() {
        return courseRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CourseDTO getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
        return toDTO(course);
    }

    @Override
    public CourseDTO createCourse(CourseDTO dto) {
        Course course = toEntity(dto);
        Course saved = courseRepository.save(course);
        return toDTO(saved);
    }

    @Override
    public CourseDTO updateCourse(Long id, CourseDTO dto) {
        Course existing = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
        existing.setTitle(dto.getTitle());
        existing.setDescription(dto.getDescription());
        Course updated = courseRepository.save(existing);
        return toDTO(updated);
    }

    @Override
    public void deleteCourse(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Course not found with id: " + id);
        }
        courseRepository.deleteById(id);
    }

    // ── Mapping helpers ──

    private CourseDTO toDTO(Course course) {
        return CourseDTO.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .createdAt(course.getCreatedAt())
                .build();
    }

    private Course toEntity(CourseDTO dto) {
        return Course.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .build();
    }
}
