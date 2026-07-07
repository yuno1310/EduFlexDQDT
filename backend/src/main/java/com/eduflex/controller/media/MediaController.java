package com.eduflex.controller.media;

import com.eduflex.service.media.SupabaseStorageService;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;

@RestController
@RequestMapping("/api/media")
public class MediaController {

  @Autowired
  private SupabaseStorageService storageService;

  @Autowired
  private DSLContext dsl;

  @PostMapping(value = "/users/{userId}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<?> uploadAvatar(@PathVariable UUID userId, @RequestParam("file") MultipartFile file) {
    try {
      String url = storageService.uploadFile(file, "avatars");
      dsl.update(com.eduflex.generated.tables.Users.USERS)
          .set(com.eduflex.generated.tables.Users.USERS.AVATAR_URL, url)
          .where(com.eduflex.generated.tables.Users.USERS.USER_ID.eq(userId))
          .execute();
      return ResponseEntity.ok(java.util.Map.of("success", true, "url", url));
    } catch (Exception e) {
      return ResponseEntity.badRequest().body("Error: " + e.getMessage());
    }
  }

  @PostMapping(value = "/courses/{courseId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<?> uploadCourseImage(@PathVariable UUID courseId, @RequestParam("file") MultipartFile file) {
    try {
      String url = storageService.uploadFile(file, "course-images");
      dsl.update(com.eduflex.generated.tables.Courses.COURSES)
          .set(com.eduflex.generated.tables.Courses.COURSES.IMAGE_URL, url)
          .where(com.eduflex.generated.tables.Courses.COURSES.COURSE_ID.eq(courseId))
          .execute();
      return ResponseEntity.ok(java.util.Map.of("success", true, "url", url));
    } catch (Exception e) {
      return ResponseEntity.badRequest().body("Error: " + e.getMessage());
    }
  }

  @PostMapping(value = "/lessons/{lessonId}/video", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<?> uploadLessonVideo(@PathVariable UUID lessonId, @RequestParam("file") MultipartFile file) {
    try {
      String url = storageService.uploadFile(file, "lesson-videos");
      dsl.update(com.eduflex.generated.tables.Lesson.LESSON)
          .set(com.eduflex.generated.tables.Lesson.LESSON.VIDEO_URL, url)
          .where(com.eduflex.generated.tables.Lesson.LESSON.LESSON_ID.eq(lessonId))
          .execute();
      return ResponseEntity.ok(java.util.Map.of("success", true, "url", url));
    } catch (Exception e) {
      return ResponseEntity.badRequest().body("Error: " + e.getMessage());
    }
  }
}
