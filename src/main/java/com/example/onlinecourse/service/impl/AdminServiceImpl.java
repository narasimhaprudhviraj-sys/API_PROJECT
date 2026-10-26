package com.example.onlinecourse.service.impl;

import com.example.onlinecourse.config.JwtService;
import com.example.onlinecourse.dto.DashboardStatsDTO;
import com.example.onlinecourse.dto.LoginRequest;
import com.example.onlinecourse.dto.LoginResponse;
import com.example.onlinecourse.entity.Admin;
import com.example.onlinecourse.entity.Faculty;
import com.example.onlinecourse.exception.InvalidCredentialsException;
import com.example.onlinecourse.exception.ResourceNotFoundException;
import com.example.onlinecourse.repository.AdminRepository;
import com.example.onlinecourse.repository.BranchRepository;
import com.example.onlinecourse.repository.CourseRepository;
import com.example.onlinecourse.repository.FacultyRepository;
import com.example.onlinecourse.repository.MinorStreamRepository;
import com.example.onlinecourse.repository.RegistrationRepository;
import com.example.onlinecourse.repository.RegistrationWindowRepository;
import com.example.onlinecourse.repository.StudentRepository;
import com.example.onlinecourse.service.AdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service implementation for administrator operations, dashboard metrics,
 * and authenticated login for Admin and Faculty roles via JWT.
 */
@Service
public class AdminServiceImpl implements AdminService {

    private static final Logger logger = LoggerFactory.getLogger(AdminServiceImpl.class);

    private final AdminRepository adminRepository;
    private final FacultyRepository facultyRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final RegistrationRepository registrationRepository;
    private final BranchRepository branchRepository;
    private final MinorStreamRepository minorStreamRepository;
    private final RegistrationWindowRepository registrationWindowRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AdminServiceImpl(AdminRepository adminRepository,
                            FacultyRepository facultyRepository,
                            StudentRepository studentRepository,
                            CourseRepository courseRepository,
                            RegistrationRepository registrationRepository,
                            BranchRepository branchRepository,
                            MinorStreamRepository minorStreamRepository,
                            RegistrationWindowRepository registrationWindowRepository,
                            BCryptPasswordEncoder passwordEncoder,
                            JwtService jwtService) {
        this.adminRepository = adminRepository;
        this.facultyRepository = facultyRepository;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.registrationRepository = registrationRepository;
        this.branchRepository = branchRepository;
        this.minorStreamRepository = minorStreamRepository;
        this.registrationWindowRepository = registrationWindowRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /**
     * Authenticates an administrator using BCrypt and issues a signed JWT token.
     */
    @Override
    public LoginResponse loginAdmin(LoginRequest loginRequest) {
        String email = loginRequest.getEmail().trim().toLowerCase();
        logger.info("Admin login attempt for email: {}", email);

        Admin admin = adminRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password."));

        if (!passwordEncoder.matches(loginRequest.getPassword(), admin.getPassword())) {
            logger.warn("Admin login failed (bad password) for email: {}", email);
            throw new InvalidCredentialsException("Invalid email or password.");
        }

        logger.info("Admin login successful for email: {}", email);
        String jwtToken = jwtService.generateToken(admin.getEmail(), admin.getAdminId(), "ROLE_ADMIN", admin.getName());
        return new LoginResponse(true, "Admin login successful", "ADMIN",
                admin.getAdminId(), admin.getName(), admin.getEmail(), jwtToken);
    }

    /**
     * Authenticates a faculty member using BCrypt and issues a signed JWT token.
     */
    @Override
    public LoginResponse loginFaculty(LoginRequest loginRequest) {
        String email = loginRequest.getEmail().trim().toLowerCase();
        logger.info("Faculty login attempt for email: {}", email);

        Faculty faculty = facultyRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password."));

        if (!passwordEncoder.matches(loginRequest.getPassword(), faculty.getPassword())) {
            logger.warn("Faculty login failed (bad password) for email: {}", email);
            throw new InvalidCredentialsException("Invalid email or password.");
        }

        if (!faculty.isActive()) {
            logger.warn("Faculty login rejected — account inactive for email: {}", email);
            throw new InvalidCredentialsException("Faculty account is inactive.");
        }

        logger.info("Faculty login successful for email: {}", email);
        String jwtToken = jwtService.generateToken(faculty.getEmail(), faculty.getId(), "ROLE_FACULTY", faculty.getName());
        LoginResponse response = new LoginResponse(
                true,
                "Faculty login successful",
                "FACULTY",
                faculty.getId(),
                faculty.getName(),
                faculty.getEmail(),
                jwtToken
        );
        response.setFacultyCode(faculty.getFacultyCode());
        response.setDepartment(faculty.getDepartment());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public Admin getAdminById(Long id) {
        return adminRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Admin> getAllAdmins() {
        return adminRepository.findAll();
    }

    /**
     * Computes dashboard statistics with exact argument mapping matching DashboardStatsDTO field order:
     * (totalStudents, totalFaculty, totalBranches, totalMinorStreams, totalCourses, activeWindows, totalRegistrations, availableCourses)
     */
    @Override
    @Transactional(readOnly = true)
    public DashboardStatsDTO getDashboardStats() {
        long totalStudents      = studentRepository.count();
        long totalFaculty       = facultyRepository.count();
        long totalBranches      = branchRepository.count();
        long totalMinorStreams  = minorStreamRepository.count();
        long totalCourses       = courseRepository.count();
        long activeWindows      = registrationWindowRepository.findAll()
                .stream()
                .filter(w -> "OPEN".equalsIgnoreCase(w.getStatus()) && w.isActive())
                .count();
        long totalRegistrations = registrationRepository.count();
        long availableCourses   = courseRepository.findByAvailableSeatsGreaterThan(0).size();

        return new DashboardStatsDTO(
                totalStudents,
                totalFaculty,
                totalBranches,
                totalMinorStreams,
                totalCourses,
                activeWindows,
                totalRegistrations,
                availableCourses
        );
    }
}
