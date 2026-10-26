package com.example.onlinecourse.controller;

import com.example.onlinecourse.dto.CourseDTO;
import com.example.onlinecourse.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing courses within the online course registration system.
 * Provides endpoints for CRUD operations and various filtering capabilities.
 */
@RestController
@RequestMapping("/api/courses")
@CrossOrigin(origins = "*")
public class CourseController {

    private final CourseService courseService;

    /**
     * Constructs a {@code CourseController} with the required service dependency.
     *
     * @param courseService service handling course business logic
     */
    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    /**
     * Retrieves all courses.
     *
     * @return a {@link ResponseEntity} containing the list of all courses
     */
    @GetMapping
    public ResponseEntity<List<CourseDTO>> getAllCourses() {
        return ResponseEntity.ok(courseService.getAllCourses());
    }

    /**
     * Retrieves a paginated list of courses.
     *
     * @param pageable pagination and sorting parameters
     * @return a {@link ResponseEntity} containing a {@link Page} of {@link CourseDTO}
     */
    @GetMapping("/page")
    public ResponseEntity<Page<CourseDTO>> getCoursesPage(Pageable pageable) {
        return ResponseEntity.ok(courseService.getCoursesPage(pageable));
    }

    /**
     * Retrieves a course by its ID.
     *
     * @param id the ID of the course to retrieve
     * @return a {@link ResponseEntity} containing the found course
     */
    @GetMapping("/{id}")
    public ResponseEntity<CourseDTO> getCourseById(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.getCourseById(id));
    }

    /**
     * Creates a new course.
     *
     * @param courseDTO the data transfer object with course creation details
     * @return a {@link ResponseEntity} with HTTP 201 status containing the created course
     */
    @PostMapping
    public ResponseEntity<CourseDTO> createCourse(@Valid @RequestBody CourseDTO courseDTO) {
        CourseDTO created = courseService.addCourse(courseDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Updates an existing course.
     *
     * @param id        the ID of the course to update
     * @param courseDTO the updated course data
     * @return a {@link ResponseEntity} containing the updated course
     */
    @PutMapping("/{id}")
    public ResponseEntity<CourseDTO> updateCourse(@PathVariable Long id,
                                                  @Valid @RequestBody CourseDTO courseDTO) {
        return ResponseEntity.ok(courseService.updateCourse(id, courseDTO));
    }

    /**
     * Deletes a course by its ID.
     *
     * @param id the ID of the course to delete
     * @return a {@link ResponseEntity} with HTTP 204 NO_CONTENT status
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Retrieves all courses belonging to a specific minor stream.
     *
     * @param minorStreamId the ID of the minor stream to filter by
     * @return a {@link ResponseEntity} containing the list of courses for that minor stream
     */
    @GetMapping("/minor-stream/{minorStreamId}")
    public ResponseEntity<List<CourseDTO>> getCoursesByMinorStream(@PathVariable Long minorStreamId) {
        return ResponseEntity.ok(courseService.getCoursesByMinorStream(minorStreamId));
    }

    /**
     * Retrieves all courses offered in a specific semester.
     *
     * @param semesterId the ID of the semester to filter by
     * @return a {@link ResponseEntity} containing the list of courses for that semester
     */
    @GetMapping("/semester/{semesterId}")
    public ResponseEntity<List<CourseDTO>> getCoursesBySemester(@PathVariable Long semesterId) {
        return ResponseEntity.ok(courseService.getCoursesBySemester(semesterId));
    }

    /**
     * Retrieves all courses taught by a specific faculty member.
     *
     * @param facultyId the ID of the faculty member to filter by
     * @return a {@link ResponseEntity} containing the list of courses for that faculty member
     */
    @GetMapping("/faculty/{facultyId}")
    public ResponseEntity<List<CourseDTO>> getCoursesByFaculty(@PathVariable Long facultyId) {
        return ResponseEntity.ok(courseService.getCoursesByFaculty(facultyId));
    }

    /**
     * Retrieves all currently available courses (open for registration).
     *
     * @return a {@link ResponseEntity} containing the list of available courses
     */
    @GetMapping("/available")
    public ResponseEntity<List<CourseDTO>> getAvailableCourses() {
        return ResponseEntity.ok(courseService.getAvailableCourses());
    }
}
