package com.example.onlinecourse.controller;

import com.example.onlinecourse.dto.LoginRequest;
import com.example.onlinecourse.dto.StudentResponseDTO;
import com.example.onlinecourse.service.AdminService;
import com.example.onlinecourse.service.CourseService;
import com.example.onlinecourse.service.RegistrationService;
import com.example.onlinecourse.service.StudentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for administrative operations including authentication,
 * student management, registration oversight, and dashboard statistics.
 */
@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private final AdminService adminService;
    private final StudentService studentService;
    private final RegistrationService registrationService;
    private final CourseService courseService;

    /**
     * Constructs an {@code AdminController} with the required service dependencies.
     *
     * @param adminService        service handling admin-specific business logic
     * @param studentService      service handling student operations
     * @param registrationService service handling course registrations
     * @param courseService       service handling course operations
     */
    public AdminController(AdminService adminService,
                           StudentService studentService,
                           RegistrationService registrationService,
                           CourseService courseService) {
        this.adminService = adminService;
        this.studentService = studentService;
        this.registrationService = registrationService;
        this.courseService = courseService;
    }

    /**
     * Authenticates an administrator.
     *
     * @param loginRequest the login credentials
     * @return a {@link ResponseEntity} containing the authentication response
     */
    @PostMapping("/login")
    public ResponseEntity<?> adminLogin(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(adminService.loginAdmin(loginRequest));
    }

    /**
     * Authenticates a faculty member via the admin endpoint.
     *
     * @param loginRequest the login credentials
     * @return a {@link ResponseEntity} containing the authentication response
     */
    @PostMapping("/faculty/login")
    public ResponseEntity<?> facultyLogin(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(adminService.loginFaculty(loginRequest));
    }

    /**
     * Retrieves all registered students.
     *
     * @return a {@link ResponseEntity} containing the list of all students
     */
    @GetMapping("/students")
    public ResponseEntity<List<StudentResponseDTO>> getAllStudents() {
        return ResponseEntity.ok(studentService.getAllStudents());
    }

    /**
     * Retrieves a paginated list of students.
     *
     * @param pageable pagination and sorting parameters (default size=10, sort by name)
     * @return a {@link Page} of {@link StudentResponseDTO}
     */
    @GetMapping("/students/page")
    public Page<StudentResponseDTO> getStudentsPage(
            @PageableDefault(size = 10, sort = "name") Pageable pageable) {
        return studentService.getStudentsPage(pageable);
    }

    /**
     * Retrieves all course registrations.
     *
     * @return a {@link ResponseEntity} containing all registrations
     */
    @GetMapping("/registrations")
    public ResponseEntity<?> getAllRegistrations() {
        return ResponseEntity.ok(registrationService.getAllRegistrations());
    }

    /**
     * Retrieves a paginated list of course registrations.
     *
     * @param pageable pagination parameters
     * @return a paginated result of registrations
     */
    @GetMapping("/registrations/page")
    public ResponseEntity<?> getRegistrationsPage(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(registrationService.getRegistrationsPage(pageable));
    }

    /**
     * Retrieves admin dashboard statistics.
     *
     * @return a {@link ResponseEntity} containing dashboard statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getDashboardStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    /**
     * Retrieves a paginated list of courses.
     *
     * @param pageable pagination parameters
     * @return a paginated result of courses
     */
    @GetMapping("/courses/page")
    public ResponseEntity<?> getCoursesPage(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(courseService.getCoursesPage(pageable));
    }
}
