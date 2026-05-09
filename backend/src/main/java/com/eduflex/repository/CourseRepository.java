package com.eduflex.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.eduflex.dto.CourseSearchDTO.CourseSuggestionResponse;
import com.eduflex.dto.GetCourseDTO.CourseInfo;
import com.eduflex.entity.CourseDbO;
import com.eduflex.generated.tables.Courses;
import com.eduflex.generated.tables.Enrollments;
import com.eduflex.generated.tables.records.CoursesRecord;

@Repository
public class CourseRepository {
  @Autowired
  private DSLContext dsl;

  public boolean save(CourseDbO course) {
    course.record.attach(dsl.configuration());
    if (course.record.store() > 0) {
      return true;
    } else {
      return false;
    }
  }

  public List<CourseInfo> get_course() {
    var records = dsl.select(
        Courses.COURSES.COURSE_ID,
        Courses.COURSES.TITLE,
        Courses.COURSES.LEARNING_MODEL,
        Courses.COURSES.STATUS,
        Courses.COURSES.DESCRIPTION,
        Courses.COURSES.IMAGE_URL,
        Courses.COURSES.PRICE)
        .from(Courses.COURSES)
        .fetch();
    if (records != null) {
      List<CourseInfo> list = new ArrayList<CourseInfo>();
      for (var record : records) {
        CourseInfo course = new CourseInfo(
            record.value1(), record.value2(), record.value3(), record.value4(),
            record.value5(), record.value6(), record.value7());
        list.add(course);
      }
      return list;
    } else {
      return null;
    }
  }

  public List<CourseInfo> get_active_course() {
    var records = dsl.select(
        Courses.COURSES.COURSE_ID,
        Courses.COURSES.TITLE,
        Courses.COURSES.LEARNING_MODEL,
        Courses.COURSES.STATUS,
        Courses.COURSES.DESCRIPTION,
        Courses.COURSES.IMAGE_URL,
        Courses.COURSES.PRICE)
        .from(Courses.COURSES)
        .where(Courses.COURSES.STATUS.equalIgnoreCase("active"))
        .fetch();
    if (records != null) {
      List<CourseInfo> list = new ArrayList<CourseInfo>();
      for (var record : records) {
        CourseInfo course = new CourseInfo(
            record.value1(), record.value2(), record.value3(), record.value4(),
            record.value5(), record.value6(), record.value7());
        list.add(course);
      }
      return list;
    } else {
      return null;
    }
  }

  public List<CourseSuggestionResponse> searchUnenrolledCourses(UUID userId, String keyword, int limit) {
        var c = Courses.COURSES;
        var e = Enrollments.ENROLLMENTS;

        return dsl.select(c.COURSE_ID, c.TITLE, c.IMAGE_URL)
                  .from(c)
                  .where(c.TITLE.likeIgnoreCase("%" + keyword + "%"))
                  .and(c.STATUS.equalIgnoreCase("active"))
                  .and(c.COURSE_ID.notIn(
                      dsl.select(e.COURSE_ID)
                         .from(e)
                         .where(e.USER_ID.eq(userId))
                  ))
                  .limit(limit)
                  .fetchInto(CourseSuggestionResponse.class);
    }

    public List<CourseSuggestionResponse> getMyCourses(UUID userId) {
        var c = Courses.COURSES;
        var e = Enrollments.ENROLLMENTS;

        return dsl.select(c.COURSE_ID, c.TITLE, c.IMAGE_URL)
                  .from(c)
                  .join(e).on(c.COURSE_ID.eq(e.COURSE_ID))
                  .where(e.USER_ID.eq(userId))
                  .fetchInto(CourseSuggestionResponse.class);
    }

  public boolean existsById(UUID courseId) {
    return dsl.fetchExists(
        dsl.selectOne()
            .from(Courses.COURSES)
            .where(Courses.COURSES.COURSE_ID.eq(courseId)));
  }

  public CoursesRecord find_by_id_course(UUID courseId) {
    var result = dsl.selectFrom(Courses.COURSES)
        .where(Courses.COURSES.COURSE_ID.eq(courseId))
        .fetchOne();
    return result;
  }

  public boolean deleteCourse(UUID courseId) {
    return dsl.deleteFrom(Courses.COURSES)
        .where(Courses.COURSES.COURSE_ID.eq(courseId))
        .execute() > 0;
  }

  public boolean updateCourse(UUID courseId, String title, String learningModel, String status,
                               String imageUrl, Long price, String description) {
    var update = dsl.update(Courses.COURSES);
    var step = update.set(Courses.COURSES.TITLE, title)
        .set(Courses.COURSES.LEARNING_MODEL, learningModel)
        .set(Courses.COURSES.STATUS, status)
        .set(Courses.COURSES.IMAGE_URL, imageUrl)
        .set(Courses.COURSES.PRICE, price)
        .set(Courses.COURSES.DESCRIPTION, description)
        .where(Courses.COURSES.COURSE_ID.eq(courseId));
    return step.execute() > 0;
  }

  public List<CourseSuggestionResponse> searchCoursesByKeyword(String keyword, int limit) {
    var c = com.eduflex.generated.tables.Courses.COURSES;

    return dsl.select(c.COURSE_ID, c.TITLE, c.IMAGE_URL)
        .from(c)
        .where(c.TITLE.likeIgnoreCase("%" + keyword + "%"))
        .and(c.STATUS.equalIgnoreCase("active"))
        .limit(limit)
        .fetchInto(CourseSuggestionResponse.class);
  }

  public List<CourseSuggestionResponse> semanticSearchCourses(String pgVector, int limit) {
    return dsl.fetch(
        "SELECT result_id AS \"courseId\", title, NULL::text AS \"imageUrl\" " +
        "FROM search_content({0}::vector, 0.1, {1}) " +
        "WHERE result_type = 'course'",
        pgVector, limit)
        .into(CourseSuggestionResponse.class);
  }
}
