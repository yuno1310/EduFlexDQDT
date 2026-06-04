package com.eduflex;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching 
public class EduflexDQDTApplication {

  public static void main(String[] args) {
    SpringApplication.run(EduflexDQDTApplication.class, args);
  }

}
