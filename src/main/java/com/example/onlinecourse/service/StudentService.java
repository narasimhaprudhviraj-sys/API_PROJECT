package com.example.onlinecourse.service;

import com.example.onlinecourse.dto.LoginRequest;
import com.example.onlinecourse.dto.LoginResponse;
import com.example.onlinecourse.dto.StudentDTO;
import com.example.onlinecourse.dto.StudentResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StudentService {

    StudentResponseDTO registerStudent(StudentDTO studentDTO);

    LoginResponse loginStudent(LoginRequest loginRequest);

    StudentResponseDTO getStudentById(Long id);

    List<StudentResponseDTO> getAllStudents();

    Page<StudentResponseDTO> getStudentsPage(Pageable pageable);

    StudentResponseDTO updateStudent(Long id, StudentDTO studentDTO);

    /**
     * CORE REQUIREMENT:
     * When student updates their own profile, they CANNOT alter branch, minorStream, or section.
     */
    StudentResponseDTO updateStudentSelfProfile(Long id, StudentDTO studentDTO);

    void deleteStudent(Long id);

    List<StudentResponseDTO> searchStudents(String keyword);

    StudentResponseDTO assignStudentAcademics(Long studentId, Long branchId, Long minorStreamId, Long sectionId, Long semesterId);
}
