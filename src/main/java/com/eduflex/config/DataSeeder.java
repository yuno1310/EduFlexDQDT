package com.eduflex.config;

import com.eduflex.entity.Course;
import com.eduflex.repository.CourseRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedCourses(CourseRepository courseRepository) {
        return args -> {
            if (courseRepository.count() == 0) {
                List<Course> courses = List.of(
                        Course.builder()
                                .title("Introduction to Java")
                                .description("Learn Java fundamentals including OOP, collections, and streams.")
                                .build(),
                        Course.builder()
                                .title("Spring Boot Masterclass")
                                .description("Build production-ready REST APIs with Spring Boot 3.")
                                .build(),
                        Course.builder()
                                .title("Data Structures & Algorithms")
                                .description("Master essential DSA concepts for coding interviews.")
                                .build()
                );
                courseRepository.saveAll(courses);
                System.out.println("✅ Seeded " + courses.size() + " mock courses.");
            }
        };
    }
}
