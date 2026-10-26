package com.example.onlinecourse.service;

import com.example.onlinecourse.dto.RegistrationDTO;
import com.example.onlinecourse.dto.RegistrationResponseDTO;
import com.example.onlinecourse.entity.*;
import com.example.onlinecourse.exception.*;
import com.example.onlinecourse.repository.CourseRepository;
import com.example.onlinecourse.repository.RegistrationRepository;
import com.example.onlinecourse.repository.SemesterRepository;
import com.example.onlinecourse.repository.StudentRepository;
import com.example.onlinecourse.service.impl.RegistrationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RegistrationServiceTest {

    @Mock
    private RegistrationRepository registrationRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private SemesterRepository semesterRepository;

    @Mock
    private RegistrationWindowService registrationWindowService;

    @InjectMocks
    private RegistrationServiceImpl registrationService;

    private Student student;
    private Course course;
    private Branch branch;
    private MinorStream minorStream;
    private Semester semester;
    private Registration registration;
    private RegistrationDTO registrationDTO;

    @BeforeEach
    void setUp() {
        branch = new Branch("AIML", "AI and ML", true);
        branch.setId(1L);

        minorStream = new MinorStream("MAD", "MAD Stream", branch, true);
        minorStream.setId(1L);

        semester = new Semester(3, "2025-26", true);
        semester.setId(1L);

        student = new Student();
        student.setStudentId(1L);
        student.setName("Akhil Sharma");
        student.setEmail("akhil@gmail.com");
        student.setRollNo("22A01");
        student.setBranch(branch);
        student.setMinorStream(minorStream);
        student.setCurrentSemester(semester);
        student.setStatus("ACTIVE");

        course = new Course();
        course.setCourseId(1L);
        course.setCourseCode("MAD301");
        course.setCourseName("Mobile App Dev");
        course.setStatus("ACTIVE");
        course.setCredits(4);
        course.setMaxSeats(30);
        course.setAvailableSeats(10);
        course.setBranch(branch);
        course.setMinorStream(minorStream);
        course.setSemester(semester);

        registration = new Registration(student, course, semester, LocalDateTime.now(), "REGISTERED");
        registration.setRegistrationId(10L);

        registrationDTO = new RegistrationDTO(1L, 1L);
    }

    @Test
    @DisplayName("Should successfully register a course and decrement available seats")
    void testRegisterCourse_Success() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(registrationWindowService.isWindowOpenForSemester(1L)).thenReturn(true);
        when(registrationRepository.findByStudentStudentIdAndCourseCourseIdAndSemesterId(1L, 1L, 1L))
                .thenReturn(Optional.empty());
        when(courseRepository.save(any(Course.class))).thenReturn(course);
        when(registrationRepository.save(any(Registration.class))).thenReturn(registration);

        int initialSeats = course.getAvailableSeats();

        RegistrationResponseDTO result = registrationService.registerCourse(registrationDTO);

        assertNotNull(result);
        assertEquals("REGISTERED", result.getStatus());
        assertEquals(initialSeats - 1, course.getAvailableSeats());
        verify(registrationRepository, times(1)).save(any(Registration.class));
        verify(courseRepository, times(1)).save(course);
    }

    @Test
    @DisplayName("Should throw RegistrationClosedException when window is closed")
    void testRegisterCourse_WindowClosed() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(registrationWindowService.isWindowOpenForSemester(1L)).thenReturn(false);

        assertThrows(RegistrationClosedException.class, () -> registrationService.registerCourse(registrationDTO));
        verify(registrationRepository, never()).save(any(Registration.class));
    }

    @Test
    @DisplayName("Should throw DuplicateRegistrationException when student already registered")
    void testRegisterCourse_Duplicate() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(registrationWindowService.isWindowOpenForSemester(1L)).thenReturn(true);
        when(registrationRepository.findByStudentStudentIdAndCourseCourseIdAndSemesterId(1L, 1L, 1L))
                .thenReturn(Optional.of(registration));

        assertThrows(DuplicateRegistrationException.class, () -> registrationService.registerCourse(registrationDTO));
        verify(registrationRepository, never()).save(any(Registration.class));
    }

    @Test
    @DisplayName("Should reactivate previously DROPPED registration when student re-registers")
    void testRegisterCourse_ReactivateDroppedRegistration() {
        Registration droppedReg = new Registration(student, course, semester, LocalDateTime.now().minusDays(1), "DROPPED");
        droppedReg.setRegistrationId(25L);

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(registrationWindowService.isWindowOpenForSemester(1L)).thenReturn(true);
        when(registrationRepository.findByStudentStudentIdAndCourseCourseIdAndSemesterId(1L, 1L, 1L))
                .thenReturn(Optional.of(droppedReg));
        when(registrationRepository.save(any(Registration.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(courseRepository.save(any(Course.class))).thenReturn(course);

        int initialSeats = course.getAvailableSeats();

        RegistrationResponseDTO result = registrationService.registerCourse(registrationDTO);

        assertNotNull(result);
        assertEquals("REGISTERED", result.getStatus());
        assertEquals(initialSeats - 1, course.getAvailableSeats());
        assertEquals(25L, result.getRegistrationId());
    }

    @Test
    @DisplayName("Should throw CourseFullException when no seats available")
    void testRegisterCourse_CourseFull() {
        course.setAvailableSeats(0);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(registrationWindowService.isWindowOpenForSemester(1L)).thenReturn(true);
        when(registrationRepository.findByStudentStudentIdAndCourseCourseIdAndSemesterId(1L, 1L, 1L))
                .thenReturn(Optional.empty());

        assertThrows(CourseFullException.class, () -> registrationService.registerCourse(registrationDTO));
        verify(registrationRepository, never()).save(any(Registration.class));
    }

    @Test
    @DisplayName("Should throw InvalidCourseEligibilityException when minor stream does not match")
    void testRegisterCourse_AcademicMismatch() {
        MinorStream otherStream = new MinorStream("NA", "NA Stream", branch, true);
        otherStream.setId(2L);
        course.setMinorStream(otherStream);

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(registrationWindowService.isWindowOpenForSemester(1L)).thenReturn(true);

        assertThrows(InvalidCourseEligibilityException.class, () -> registrationService.registerCourse(registrationDTO));
        verify(registrationRepository, never()).save(any(Registration.class));
    }

    @Test
    @DisplayName("Should successfully drop course, increment seats, and set status to DROPPED")
    void testDropCourse_Success() {
        when(registrationRepository.findById(10L)).thenReturn(Optional.of(registration));
        when(registrationWindowService.isWindowOpenForSemester(1L)).thenReturn(true);
        when(courseRepository.save(any(Course.class))).thenReturn(course);
        when(registrationRepository.save(any(Registration.class))).thenReturn(registration);

        int initialSeats = course.getAvailableSeats();

        registrationService.dropCourse(10L, 1L);

        assertEquals(initialSeats + 1, course.getAvailableSeats());
        assertEquals("DROPPED", registration.getStatus());
        verify(registrationRepository, times(1)).save(registration);
        verify(courseRepository, times(1)).save(course);
    }

    @Test
    @DisplayName("Should throw RegistrationClosedException when dropping course after deadline")
    void testDropCourse_WindowClosed() {
        when(registrationRepository.findById(10L)).thenReturn(Optional.of(registration));
        when(registrationWindowService.isWindowOpenForSemester(1L)).thenReturn(false);

        assertThrows(RegistrationClosedException.class, () -> registrationService.dropCourse(10L, 1L));
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    @DisplayName("Should throw UnauthorizedOperationException when student tries to drop another student's registration")
    void testDropCourse_Unauthorized() {
        when(registrationRepository.findById(10L)).thenReturn(Optional.of(registration));

        assertThrows(UnauthorizedOperationException.class, () -> registrationService.dropCourse(10L, 999L));
        verify(courseRepository, never()).save(any(Course.class));
    }
}
