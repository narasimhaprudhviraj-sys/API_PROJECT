package com.example.onlinecourse.controller;

import com.example.onlinecourse.config.UserPrincipal;
import com.example.onlinecourse.dto.RegistrationDTO;
import com.example.onlinecourse.dto.RegistrationResponseDTO;
import com.example.onlinecourse.exception.UnauthorizedOperationException;
import com.example.onlinecourse.service.RegistrationService;
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
 * REST controller for managing course registrations within the online course registration system.
 * Provides endpoints for registering courses, retrieving registrations by various filters,
 * and cancelling or dropping registrations.
 */
@RestController
@RequestMapping("/api/registrations")
@CrossOrigin(origins = "*")
public class RegistrationController {

    private final RegistrationService registrationService;

    /**
     * Constructs a {@code RegistrationController} with the required service dependency.
     *
     * @param registrationService service handling course registration business logic
     */
    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    /**
     * Registers a student for a course.
     * If caller is an authenticated student, their verified student ID is used to prevent tampering.
     *
     * @param registrationDTO the data transfer object containing registration details
     * @param authentication the security context authentication
     * @return a {@link ResponseEntity} with HTTP 201 status containing the created registration
     */
    @PostMapping
    public ResponseEntity<RegistrationResponseDTO> registerCourse(
            @Valid @RequestBody RegistrationDTO registrationDTO,
            Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            if ("ROLE_STUDENT".equalsIgnoreCase(principal.getRole())) {
                registrationDTO.setStudentId(principal.getUserId());
            }
        }
        RegistrationResponseDTO created = registrationService.registerCourse(registrationDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Retrieves all course registrations.
     *
     * @return a {@link ResponseEntity} containing the list of all registrations
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RegistrationResponseDTO>> getAllRegistrations() {
        return ResponseEntity.ok(registrationService.getAllRegistrations());
    }

    /**
     * Retrieves a paginated list of course registrations.
     *
     * @param pageable pagination and sorting parameters
     * @return a {@link ResponseEntity} containing a {@link Page} of {@link RegistrationResponseDTO}
     */
    @GetMapping("/page")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<RegistrationResponseDTO>> getRegistrationsPage(Pageable pageable) {
        return ResponseEntity.ok(registrationService.getRegistrationsPage(pageable));
    }

    /**
     * Retrieves a specific registration by its ID.
     *
     * @param id the ID of the registration to retrieve
     * @return a {@link ResponseEntity} containing the found registration
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RegistrationResponseDTO> getRegistrationById(@PathVariable Long id) {
        return ResponseEntity.ok(registrationService.getRegistrationById(id));
    }

    /**
     * Retrieves all registrations for a specific student.
     * Ensures students can only view their own registrations.
     *
     * @param studentId the ID of the student
     * @param authentication the security context authentication
     * @return a {@link ResponseEntity} containing the list of registrations for that student
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<RegistrationResponseDTO>> getStudentRegistrations(
            @PathVariable Long studentId,
            Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            if ("ROLE_STUDENT".equalsIgnoreCase(principal.getRole()) && !principal.getUserId().equals(studentId)) {
                throw new UnauthorizedOperationException("Access denied: You cannot view another student's registrations.");
            }
        }
        return ResponseEntity.ok(registrationService.getStudentRegistrations(studentId));
    }

    /**
     * Retrieves all registrations for a specific course.
     *
     * @param courseId the ID of the course
     * @return a {@link ResponseEntity} containing the list of registrations for that course
     */
    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RegistrationResponseDTO>> getCourseRegistrations(
            @PathVariable Long courseId) {
        return ResponseEntity.ok(registrationService.getCourseRegistrations(courseId));
    }

    /**
     * Cancels a registration by its ID (admin operation).
     *
     * @param id the ID of the registration to cancel
     * @return a {@link ResponseEntity} with HTTP 204 NO_CONTENT status
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> cancelRegistration(@PathVariable Long id) {
        registrationService.cancelRegistration(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Allows a student to drop a registered course.
     *
     * @param id        the ID of the registration to drop
     * @param studentId the ID of the student dropping the course
     * @param authentication the security context authentication
     * @return a {@link ResponseEntity} with HTTP 204 NO_CONTENT status
     */
    @DeleteMapping("/{id}/drop/{studentId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Void> dropCourse(@PathVariable Long id,
                                           @PathVariable Long studentId,
                                           Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            if ("ROLE_STUDENT".equalsIgnoreCase(principal.getRole()) && !principal.getUserId().equals(studentId)) {
                throw new UnauthorizedOperationException("Access denied: You cannot drop courses for another student.");
            }
        }
        registrationService.dropCourse(id, studentId);
        return ResponseEntity.noContent().build();
    }
}
