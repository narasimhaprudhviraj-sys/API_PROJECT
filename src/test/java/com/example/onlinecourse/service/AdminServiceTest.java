package com.example.onlinecourse.service;

import com.example.onlinecourse.config.JwtService;
import com.example.onlinecourse.dto.DashboardStatsDTO;
import com.example.onlinecourse.dto.LoginRequest;
import com.example.onlinecourse.dto.LoginResponse;
import com.example.onlinecourse.entity.Admin;
import com.example.onlinecourse.entity.Faculty;
import com.example.onlinecourse.exception.InvalidCredentialsException;
import com.example.onlinecourse.repository.*;
import com.example.onlinecourse.service.impl.AdminServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminServiceTest {

    @Mock
    private AdminRepository adminRepository;
    @Mock
    private FacultyRepository facultyRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private RegistrationRepository registrationRepository;
    @Mock
    private BranchRepository branchRepository;
    @Mock
    private MinorStreamRepository minorStreamRepository;
    @Mock
    private RegistrationWindowRepository registrationWindowRepository;

    private BCryptPasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private AdminServiceImpl adminService;

    private Admin admin;
    private Faculty faculty;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        jwtService = new JwtService();
        adminService = new AdminServiceImpl(
                adminRepository,
                facultyRepository,
                studentRepository,
                courseRepository,
                registrationRepository,
                branchRepository,
                minorStreamRepository,
                registrationWindowRepository,
                passwordEncoder,
                jwtService
        );

        admin = new Admin("System Administrator", "admin@college.edu", passwordEncoder.encode("admin123"));
        admin.setAdminId(1L);

        faculty = new Faculty("FAC001", "Dr. Ravi Kumar", "ravi.kumar@college.edu", passwordEncoder.encode("faculty123"), "AIML", "Professor", "9876500001", true);
        faculty.setId(1L);
    }

    @Test
    @DisplayName("Should successfully authenticate admin with BCrypt password and generate JWT")
    void testLoginAdmin_Success() {
        LoginRequest request = new LoginRequest("admin@college.edu", "admin123");
        when(adminRepository.findByEmail("admin@college.edu")).thenReturn(Optional.of(admin));

        LoginResponse response = adminService.loginAdmin(request);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("ADMIN", response.getRole());
        assertEquals("System Administrator", response.getName());
        assertNotNull(response.getToken());
        assertFalse(response.getToken().isBlank());
    }

    @Test
    @DisplayName("Should reject admin login with invalid password")
    void testLoginAdmin_InvalidPassword() {
        LoginRequest request = new LoginRequest("admin@college.edu", "wrongpassword");
        when(adminRepository.findByEmail("admin@college.edu")).thenReturn(Optional.of(admin));

        assertThrows(InvalidCredentialsException.class, () -> adminService.loginAdmin(request));
    }

    @Test
    @DisplayName("Should successfully authenticate faculty and issue JWT")
    void testLoginFaculty_Success() {
        LoginRequest request = new LoginRequest("ravi.kumar@college.edu", "faculty123");
        when(facultyRepository.findByEmailIgnoreCase("ravi.kumar@college.edu")).thenReturn(Optional.of(faculty));

        LoginResponse response = adminService.loginFaculty(request);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("FACULTY", response.getRole());
        assertEquals("Dr. Ravi Kumar", response.getName());
        assertEquals("FAC001", response.getFacultyCode());
        assertNotNull(response.getToken());
    }

    @Test
    @DisplayName("Should calculate correct dashboard stats with proper field ordering")
    void testGetDashboardStats_Success() {
        when(studentRepository.count()).thenReturn(5L);
        when(facultyRepository.count()).thenReturn(5L);
        when(branchRepository.count()).thenReturn(3L);
        when(minorStreamRepository.count()).thenReturn(6L);
        when(courseRepository.count()).thenReturn(12L);
        when(courseRepository.findByAvailableSeatsGreaterThan(0)).thenReturn(Collections.emptyList());
        when(registrationRepository.count()).thenReturn(3L);
        when(registrationWindowRepository.findAll()).thenReturn(Collections.emptyList());

        DashboardStatsDTO stats = adminService.getDashboardStats();

        assertNotNull(stats);
        assertEquals(5L, stats.getTotalStudents());
        assertEquals(5L, stats.getTotalFaculty());
        assertEquals(3L, stats.getTotalBranches());
        assertEquals(6L, stats.getTotalMinorStreams());
        assertEquals(12L, stats.getTotalCourses());
        assertEquals(3L, stats.getTotalRegistrations());
    }
}
