package com.example.onlinecourse.service;

import com.example.onlinecourse.dto.CourseDTO;
import com.example.onlinecourse.entity.Course;
import com.example.onlinecourse.exception.DuplicateRegistrationException;
import com.example.onlinecourse.repository.*;
import com.example.onlinecourse.service.impl.CourseServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;
    @Mock
    private BranchRepository branchRepository;
    @Mock
    private MinorStreamRepository minorStreamRepository;
    @Mock
    private SemesterRepository semesterRepository;
    @Mock
    private FacultyRepository facultyRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private RegistrationRepository registrationRepository;

    @InjectMocks
    private CourseServiceImpl courseService;

    private Course course;
    private CourseDTO courseDTO;

    @BeforeEach
    void setUp() {
        course = new Course();
        course.setCourseId(1L);
        course.setCourseCode("CS101");
        course.setCourseName("Java Programming");
        course.setDescription("Core Java basics");
        course.setDepartment("CSE");
        course.setCredits(4);
        course.setFacultyName("Prof. Ravi Kumar");
        course.setMaxSeats(30);
        course.setAvailableSeats(30);
        course.setDuration("12 Weeks");
        course.setStatus("ACTIVE");

        courseDTO = new CourseDTO();
        courseDTO.setCourseId(1L);
        courseDTO.setCourseCode("CS101");
        courseDTO.setCourseName("Java Programming");
        courseDTO.setDescription("Core Java basics");
        courseDTO.setDepartment("CSE");
        courseDTO.setCredits(4);
        courseDTO.setFacultyName("Prof. Ravi Kumar");
        courseDTO.setMaxSeats(30);
        courseDTO.setAvailableSeats(30);
        courseDTO.setDuration("12 Weeks");
        courseDTO.setStatus("ACTIVE");
    }

    @Test
    @DisplayName("Should successfully add a new course")
    void testAddCourse_Success() {
        when(courseRepository.existsByCourseCode("CS101")).thenReturn(false);
        when(courseRepository.save(any(Course.class))).thenReturn(course);

        CourseDTO result = courseService.addCourse(courseDTO);

        assertNotNull(result);
        assertEquals("CS101", result.getCourseCode());
        assertEquals("Java Programming", result.getCourseName());
        assertEquals(30, result.getAvailableSeats());
        verify(courseRepository, times(1)).save(any(Course.class));
    }

    @Test
    @DisplayName("Should throw DuplicateRegistrationException when course code already exists")
    void testAddCourse_DuplicateCode() {
        when(courseRepository.existsByCourseCode("CS101")).thenReturn(true);

        assertThrows(DuplicateRegistrationException.class, () -> courseService.addCourse(courseDTO));
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    @DisplayName("Should find course by ID successfully")
    void testGetCourseById_Success() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

        CourseDTO result = courseService.getCourseById(1L);

        assertNotNull(result);
        assertEquals("CS101", result.getCourseCode());
        verify(courseRepository, times(1)).findById(1L);
    }
}
