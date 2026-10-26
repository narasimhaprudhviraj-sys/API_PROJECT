package com.example.onlinecourse.service;

import com.example.onlinecourse.dto.CourseDTO;
import com.example.onlinecourse.dto.FacultyDTO;
import com.example.onlinecourse.dto.SectionDTO;
import com.example.onlinecourse.dto.StudentResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface FacultyService {
    List<FacultyDTO> getAllFaculty();
    Page<FacultyDTO> getFacultyPage(Pageable pageable);
    FacultyDTO getFacultyById(Long id);
    FacultyDTO createFaculty(FacultyDTO dto);
    FacultyDTO updateFaculty(Long id, FacultyDTO dto);
    void deleteFaculty(Long id);
    void toggleFacultyStatus(Long id);

    // Faculty dashboard queries restricted to assigned scope
    List<SectionDTO> getAssignedSections(Long facultyId);
    List<CourseDTO> getAssignedCourses(Long facultyId);
    List<StudentResponseDTO> getAssignedStudents(Long facultyId);
}
