package com.example.onlinecourse.service;

import com.example.onlinecourse.config.JwtService;
import com.example.onlinecourse.dto.LoginRequest;
import com.example.onlinecourse.dto.LoginResponse;
import com.example.onlinecourse.dto.StudentDTO;
import com.example.onlinecourse.dto.StudentResponseDTO;
import com.example.onlinecourse.entity.Branch;
import com.example.onlinecourse.entity.MinorStream;
import com.example.onlinecourse.entity.Section;
import com.example.onlinecourse.entity.Semester;
import com.example.onlinecourse.entity.Student;
import com.example.onlinecourse.exception.AcademicMismatchException;
import com.example.onlinecourse.exception.DuplicateRegistrationException;
import com.example.onlinecourse.exception.InvalidCredentialsException;
import com.example.onlinecourse.exception.UnauthorizedOperationException;
import com.example.onlinecourse.repository.*;
import com.example.onlinecourse.service.impl.StudentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;
    @Mock
    private BranchRepository branchRepository;
    @Mock
    private MinorStreamRepository minorStreamRepository;
    @Mock
    private SectionRepository sectionRepository;
    @Mock
    private SemesterRepository semesterRepository;

    private JwtService jwtService;
    private BCryptPasswordEncoder passwordEncoder;
    private StudentServiceImpl studentService;

    private Student student;
    private StudentDTO studentDTO;
    private Branch aimlBranch;
    private Branch cseBranch;
    private MinorStream madStream;
    private Section madSection;
    private Semester sem3;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        jwtService = new JwtService();
        studentService = new StudentServiceImpl(
                studentRepository,
                branchRepository,
                minorStreamRepository,
                sectionRepository,
                semesterRepository,
                passwordEncoder,
                jwtService
        );

        aimlBranch = new Branch("AIML", "Artificial Intelligence", true);
        aimlBranch.setId(1L);

        cseBranch = new Branch("CSE", "Computer Science", true);
        cseBranch.setId(2L);

        madStream = new MinorStream("MAD", "MAD Stream", aimlBranch, true);
        madStream.setId(1L);

        madSection = new Section("MAD-1", aimlBranch, madStream, "2025-26", null, true);
        madSection.setId(1L);

        sem3 = new Semester(3, "2025-26", true);
        sem3.setId(1L);

        student = new Student("Akhil Sharma", "akhil@gmail.com", passwordEncoder.encode("12345"), "22A01", "AIML", 2, "9876543210");
        student.setStudentId(1L);
        student.setBranch(aimlBranch);
        student.setMinorStream(madStream);
        student.setSection(madSection);
        student.setCurrentSemester(sem3);

        studentDTO = new StudentDTO(1L, "Akhil Sharma", "akhil@gmail.com", "12345", "22A01", "AIML", 2, "9876543210");
    }

    @Test
    @DisplayName("Should successfully register a new student")
    void testRegisterStudent_Success() {
        when(studentRepository.existsByEmail("akhil@gmail.com")).thenReturn(false);
        when(studentRepository.existsByRollNo("22A01")).thenReturn(false);
        when(studentRepository.save(any(Student.class))).thenReturn(student);

        StudentResponseDTO result = studentService.registerStudent(studentDTO);

        assertNotNull(result);
        assertEquals("Akhil Sharma", result.getName());
        assertEquals("akhil@gmail.com", result.getEmail());
        assertEquals("22A01", result.getRollNo());
        verify(studentRepository, times(1)).save(any(Student.class));
    }

    @Test
    @DisplayName("Should throw DuplicateRegistrationException when email exists")
    void testRegisterStudent_DuplicateEmail() {
        when(studentRepository.existsByEmail("akhil@gmail.com")).thenReturn(true);

        assertThrows(DuplicateRegistrationException.class, () -> studentService.registerStudent(studentDTO));
        verify(studentRepository, never()).save(any(Student.class));
    }

    @Test
    @DisplayName("Should login successfully with correct credentials and issue JWT")
    void testLoginStudent_Success() {
        LoginRequest loginRequest = new LoginRequest("akhil@gmail.com", "12345");
        when(studentRepository.findByEmail("akhil@gmail.com")).thenReturn(Optional.of(student));

        LoginResponse response = studentService.loginStudent(loginRequest);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("STUDENT", response.getRole());
        assertEquals("Akhil Sharma", response.getName());
        assertNotNull(response.getToken());
        assertFalse(response.getToken().isBlank());
    }

    @Test
    @DisplayName("Should throw InvalidCredentialsException when password does not match")
    void testLoginStudent_InvalidPassword() {
        LoginRequest loginRequest = new LoginRequest("akhil@gmail.com", "wrongpassword");
        when(studentRepository.findByEmail("akhil@gmail.com")).thenReturn(Optional.of(student));

        assertThrows(InvalidCredentialsException.class, () -> studentService.loginStudent(loginRequest));
    }

    @Test
    @DisplayName("Student cannot change assigned Minor Stream by themselves")
    void testUpdateStudentSelfProfile_MinorStreamLocked() {
        StudentDTO selfUpdateDTO = new StudentDTO();
        selfUpdateDTO.setName("Akhil Sharma Updated");
        selfUpdateDTO.setPhone("9999999999");
        selfUpdateDTO.setMinorStreamId(99L); // Student attempts to change minor stream

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

        assertThrows(UnauthorizedOperationException.class, () ->
                studentService.updateStudentSelfProfile(1L, selfUpdateDTO));
    }

    @Test
    @DisplayName("Should throw AcademicMismatchException when assigning section/stream from different branch")
    void testAssignStudentAcademics_AcademicMismatch() {
        // Stream belonging to AIML, but Section belonging to CSE
        Section cseSection = new Section("CSE-1", cseBranch, null, "2025-26", null, true);
        cseSection.setId(2L);

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(branchRepository.findById(1L)).thenReturn(Optional.of(aimlBranch));
        when(minorStreamRepository.findById(1L)).thenReturn(Optional.of(madStream));
        when(sectionRepository.findById(2L)).thenReturn(Optional.of(cseSection));
        when(semesterRepository.findById(1L)).thenReturn(Optional.of(sem3));

        assertThrows(AcademicMismatchException.class, () ->
                studentService.assignStudentAcademics(1L, 1L, 1L, 2L, 1L));
    }
}
