package com.example.onlinecourse.service;

import com.example.onlinecourse.dto.CourseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CourseService {

    CourseDTO addCourse(CourseDTO courseDTO);

    CourseDTO updateCourse(Long id, CourseDTO courseDTO);

    void deleteCourse(Long id);

    void toggleCourseStatus(Long id);

    CourseDTO getCourseById(Long id);

    List<CourseDTO> getAllCourses();

    Page<CourseDTO> getCoursesPage(Pageable pageable);

    List<CourseDTO> searchCourses(String keyword);

    List<CourseDTO> filterCoursesByDepartment(String department);

    List<CourseDTO> getAvailableCourses();

    List<CourseDTO> filterCourses(String keyword, String department, boolean onlyAvailable);

    /**
     * CORE REQUIREMENT: Return eligible courses for student based on
     * Branch + Fixed Minor Stream + Semester. Also marks courses already registered.
     */
    List<CourseDTO> getEligibleCoursesForStudent(Long studentId);

    List<CourseDTO> getCoursesByMinorStream(Long minorStreamId);

    List<CourseDTO> getCoursesBySemester(Long semesterId);

    List<CourseDTO> getCoursesByFaculty(Long facultyId);
}
