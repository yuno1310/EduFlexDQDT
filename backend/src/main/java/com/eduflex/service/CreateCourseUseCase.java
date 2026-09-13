package com.eduflex.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.CacheEvict;
import com.eduflex.RabbitMQ.ContentChangedEvent;
import com.eduflex.RabbitMQ.ContentEventPublisher;

import com.eduflex.dto.CreateCourseDTO.CreateCourseRequest;
import com.eduflex.dto.CreateCourseDTO.CreateCourseResponse;
import com.eduflex.entity.CourseDbO;
import com.eduflex.repository.CourseRepository;

@Service
public class CreateCourseUseCase {
  @Autowired
  private CourseRepository courseRepository;
  @Autowired
  private ContentEventPublisher contentEvents;

  @CacheEvict(value = {"courseCatalog", "semanticSearch"}, allEntries = true)
  public CreateCourseResponse execute(CreateCourseRequest request) {
    var course = new CourseDbO(
        request.title(),
        request.learningModel(),
        request.status(),
        request.description(),
        request.imageUrl(),
        request.price()
    );
    if (courseRepository.save(course) == true) {
      contentEvents.publish(ContentChangedEvent.ContentType.COURSE, course.record.getCourseId());
      return new CreateCourseResponse(true, "Create a new course sucessfully");
    } else {
      return new CreateCourseResponse(false, "Failed to create new course");
    }
  }
}
