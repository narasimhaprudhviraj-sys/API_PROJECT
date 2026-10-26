package com.example.onlinecourse.controller;

import com.example.onlinecourse.config.UserPrincipal;
import com.example.onlinecourse.dto.CourseDTO;
import com.example.onlinecourse.dto.LoginRequest;
import com.example.onlinecourse.dto.StudentDTO;
import com.example.onlinecourse.dto.StudentResponseDTO;
import com.example.onlinecourse.exception.UnauthorizedOperationException;
import com.example.onlinecourse.service.CourseService;
import com.example.onlinecourse.service.RegistrationService;
import com.example.onlinecourse.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing students within the online course registration system.
 * Provides endpoints for authentication, profile management, academic assignment,
 * eligible course retrieval, and registration queries.
 */
@RestController
@RequestMapping("/api/students")
@CrossOrigin(origins = "*")
public class StudentController {

    private final StudentService studentService;
    private final CourseService courseService;
    private final RegistrationService registrationService;

    /**
     * Constructs a {@code StudentController} with the required service dependencies.
     *
     * @param studentService      service handling student business logic
     * @param courseService       service handling course business logic
     * @param registrationService service handling course registration business logic
     */
    public StudentController(StudentService studentService,
                             CourseService courseService,
                             RegistrationService registrationService) {
        this.studentService = studentService;
        this.courseService = courseService;
        this.registrationService = registrationService;
    }

    /**
     * Authenticates a student.
     *
     * @param loginRequest the login credentials
     * @return a {@link ResponseEntity} containing the authentication response
     */
    @PostMapping("/login")
    public ResponseEntity<?> loginStudent(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(studentService.loginStudent(loginRequest));
    }

    /**
     * Retrieves all students.
     *
     * @return a {@link ResponseEntity} containing the list of all students
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<StudentResponseDTO>> getAllStudents() {
        return ResponseEntity.ok(studentService.getAllStudents());
    }

    /**
     * Retrieves a paginated list of students.
     *
     * @param pageable pagination and sorting parameters
     * @return a {@link Page} of {@link StudentResponseDTO}
     */
    @GetMapping("/page")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<StudentResponseDTO>> getStudentsPage(Pageable pageable) {
        return ResponseEntity.ok(studentService.getStudentsPage(pageable));
    }

    /**
     * Retrieves a student by their ID.
     *
     * @param id the ID of the student to retrieve
     * @return a {@link ResponseEntity} containing the found student
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('STUDENT','ADMIN')")
    public ResponseEntity<StudentResponseDTO> getStudentById(@PathVariable Long id,
                                                            Authentication authentication) {
        verifyStudentAccess(authentication, id);
        return ResponseEntity.ok(studentService.getStudentById(id));
    }

    /**
     * Registers a new student.
     *
     * @param studentDTO the data transfer object with student registration details
     * @return a {@link ResponseEntity} with HTTP 201 status containing the created student
     */
    @PostMapping("/register")
    public ResponseEntity<StudentResponseDTO> createStudent(@Valid @RequestBody StudentDTO studentDTO) {
        StudentResponseDTO created = studentService.registerStudent(studentDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Updates a student's information (admin use).
     *
     * @param id         the ID of the student to update
     * @param studentDTO the updated student data
     * @return a {@link ResponseEntity} containing the updated student
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StudentResponseDTO> updateStudent(@PathVariable Long id,
                                                            @Valid @RequestBody StudentDTO studentDTO) {
        return ResponseEntity.ok(studentService.updateStudent(id, studentDTO));
    }

    /**
     * Allows a student to update their own profile (restricted: minor stream cannot be changed).
     *
     * @param id         the ID of the student
     * @param studentDTO the updated profile data
     * @return a {@link ResponseEntity} containing the updated student profile
     */
    @PutMapping("/{id}/self")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<StudentResponseDTO> updateStudentSelf(@PathVariable Long id,
                                                                @Valid @RequestBody StudentDTO studentDTO,
                                                                Authentication authentication) {
        verifyStudentAccess(authentication, id);
        return ResponseEntity.ok(studentService.updateStudentSelfProfile(id, studentDTO));
    }

    /**
     * Assigns academic details (branch, minor stream, section, semester) to a student (admin use).
     *
     * @param id         the ID of the student
     * @param studentDTO the DTO containing branchId, minorStreamId, sectionId, and semesterId
     * @return a {@link ResponseEntity} containing the updated student with assigned academics
     */
    @PutMapping("/{id}/academics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StudentResponseDTO> assignAcademics(@PathVariable Long id,
                                                              @RequestBody StudentDTO studentDTO) {
        return ResponseEntity.ok(studentService.assignStudentAcademics(
                id,
                studentDTO.getBranchId(),
                studentDTO.getMinorStreamId(),
                studentDTO.getSectionId(),
                studentDTO.getSemesterId()
        ));
    }

    /**
     * Deletes a student by their ID.
     *
     * @param id the ID of the student to delete
     * @return a {@link ResponseEntity} with HTTP 204 NO_CONTENT status
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Retrieves courses eligible for a student to register based on their academic profile.
     *
     * @param id the ID of the student
     * @return a {@link ResponseEntity} containing the list of eligible courses
     */
    @GetMapping("/{id}/eligible-courses")
    @PreAuthorize("hasAnyRole('STUDENT','ADMIN')")
    public ResponseEntity<List<CourseDTO>> getEligibleCourses(@PathVariable Long id,
                                                             Authentication authentication) {
        verifyStudentAccess(authentication, id);
        return ResponseEntity.ok(courseService.getEligibleCoursesForStudent(id));
    }

    /**
     * Retrieves all course registrations for a specific student.
     *
     * @param id the ID of the student
     * @return a {@link ResponseEntity} containing the list of registrations
     */
    @GetMapping("/{id}/registrations")
    @PreAuthorize("hasAnyRole('STUDENT','ADMIN')")
    public ResponseEntity<?> getStudentRegistrations(@PathVariable Long id,
                                                     Authentication authentication) {
        verifyStudentAccess(authentication, id);
        return ResponseEntity.ok(registrationService.getStudentRegistrations(id));
    }

    private void verifyStudentAccess(Authentication authentication, Long studentId) {
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            if ("ROLE_STUDENT".equalsIgnoreCase(principal.getRole()) && !principal.getUserId().equals(studentId)) {
                throw new UnauthorizedOperationException("Access denied: You can only access your own profile and registration data.");
            }
        }
    }
}
