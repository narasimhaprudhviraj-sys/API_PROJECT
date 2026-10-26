package com.example.onlinecourse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OnlineCourseApplication {

    public static void main(String[] args) {
        SpringApplication.run(OnlineCourseApplication.class, args);
        System.out.println("==========================================================");
        System.out.println("  Online Course Registration Management System Started");
        System.out.println("  Access Frontend: http://localhost:8080/index.html");
        System.out.println("  Backend REST API Base: http://localhost:8080/api");
        System.out.println("==========================================================");
    }
}
