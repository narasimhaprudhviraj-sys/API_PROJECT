package com.example.onlinecourse.controller;

import com.example.onlinecourse.config.UserPrincipal;
import com.example.onlinecourse.dto.CourseDTO;
import com.example.onlinecourse.dto.FacultyDTO;
import com.example.onlinecourse.dto.SectionDTO;
import com.example.onlinecourse.dto.StudentResponseDTO;
import com.example.onlinecourse.exception.UnauthorizedOperationException;
import com.example.onlinecourse.service.FacultyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing faculty members.
 * Enforces section-level student visibility:
 * Faculty members can only view students belonging to their assigned sections.
 */
@RestController
@RequestMapping("/api/faculty")
@CrossOrigin(origins = "*")
public class FacultyController {

    private final FacultyService facultyService;

    public FacultyController(FacultyService facultyService) {
        this.facultyService = facultyService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<FacultyDTO>> getAllFaculty() {
        return ResponseEntity.ok(facultyService.getAllFaculty());
    }

    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<FacultyDTO>> getActiveFaculty() {
        List<FacultyDTO> active = facultyService.getAllFaculty().stream()
                .filter(FacultyDTO::isActive)
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(active);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('FACULTY','ADMIN')")
    public ResponseEntity<FacultyDTO> getFacultyById(@PathVariable Long id, Authentication authentication) {
        verifyFacultyAccess(authentication, id);
        return ResponseEntity.ok(facultyService.getFacultyById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FacultyDTO> createFaculty(@Valid @RequestBody FacultyDTO facultyDTO) {
        FacultyDTO created = facultyService.createFaculty(facultyDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FacultyDTO> updateFaculty(@PathVariable Long id,
                                                     @Valid @RequestBody FacultyDTO facultyDTO) {
        return ResponseEntity.ok(facultyService.updateFaculty(id, facultyDTO));
    }

    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> toggleFacultyStatus(@PathVariable Long id) {
        facultyService.toggleFacultyStatus(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteFaculty(@PathVariable Long id) {
        facultyService.deleteFaculty(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/sections")
    @PreAuthorize("hasAnyRole('FACULTY','ADMIN')")
    public ResponseEntity<List<SectionDTO>> getAssignedSections(@PathVariable Long id, Authentication authentication) {
        verifyFacultyAccess(authentication, id);
        return ResponseEntity.ok(facultyService.getAssignedSections(id));
    }

    @GetMapping("/{id}/courses")
    @PreAuthorize("hasAnyRole('FACULTY','ADMIN')")
    public ResponseEntity<List<CourseDTO>> getAssignedCourses(@PathVariable Long id, Authentication authentication) {
        verifyFacultyAccess(authentication, id);
        return ResponseEntity.ok(facultyService.getAssignedCourses(id));
    }

    @GetMapping("/{id}/students")
    @PreAuthorize("hasAnyRole('FACULTY','ADMIN')")
    public ResponseEntity<List<StudentResponseDTO>> getAssignedStudents(@PathVariable Long id, Authentication authentication) {
        verifyFacultyAccess(authentication, id);
        return ResponseEntity.ok(facultyService.getAssignedStudents(id));
    }

    private void verifyFacultyAccess(Authentication authentication, Long facultyId) {
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            if ("ROLE_FACULTY".equalsIgnoreCase(principal.getRole()) && !principal.getUserId().equals(facultyId)) {
                throw new UnauthorizedOperationException("Access denied: You can only view your own assigned sections, courses, and students.");
            }
        }
    }
}
