package com.example.onlinecourse.service.impl;

import com.example.onlinecourse.dto.RegistrationDTO;
import com.example.onlinecourse.dto.RegistrationResponseDTO;
import com.example.onlinecourse.entity.Course;
import com.example.onlinecourse.entity.Registration;
import com.example.onlinecourse.entity.Semester;
import com.example.onlinecourse.entity.Student;
import com.example.onlinecourse.exception.ConcurrentSeatUpdateException;
import com.example.onlinecourse.exception.CourseFullException;
import com.example.onlinecourse.exception.CourseNotFoundException;
import com.example.onlinecourse.exception.DuplicateRegistrationException;
import com.example.onlinecourse.exception.InvalidCourseEligibilityException;
import com.example.onlinecourse.exception.RegistrationClosedException;
import com.example.onlinecourse.exception.RegistrationNotFoundException;
import com.example.onlinecourse.exception.ResourceNotFoundException;
import com.example.onlinecourse.exception.UnauthorizedOperationException;
import com.example.onlinecourse.repository.CourseRepository;
import com.example.onlinecourse.repository.RegistrationRepository;
import com.example.onlinecourse.repository.SemesterRepository;
import com.example.onlinecourse.repository.StudentRepository;
import com.example.onlinecourse.service.RegistrationService;
import com.example.onlinecourse.service.RegistrationWindowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for managing course registrations.
 * <p>
 * Handles the full registration lifecycle: eligibility checks, seat management,
 * drop, cancel, and retrieval operations.
 * </p>
 */
@Service
public class RegistrationServiceImpl implements RegistrationService {

    private static final Logger logger = LoggerFactory.getLogger(RegistrationServiceImpl.class);

    private final RegistrationRepository registrationRepository;
    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;
    private final SemesterRepository semesterRepository;
    private final RegistrationWindowService registrationWindowService;

    /**
     * Constructs a {@code RegistrationServiceImpl} with the required dependencies.
     *
     * @param registrationRepository   repository for registration persistence
     * @param courseRepository         repository for course data
     * @param studentRepository        repository for student data
     * @param semesterRepository       repository for semester data
     * @param registrationWindowService service for checking registration window status
     */
    public RegistrationServiceImpl(RegistrationRepository registrationRepository,
                                   CourseRepository courseRepository,
                                   StudentRepository studentRepository,
                                   SemesterRepository semesterRepository,
                                   RegistrationWindowService registrationWindowService) {
        this.registrationRepository = registrationRepository;
        this.courseRepository = courseRepository;
        this.studentRepository = studentRepository;
        this.semesterRepository = semesterRepository;
        this.registrationWindowService = registrationWindowService;
    }

    /**
     * Registers a student for a course atomically, performing all eligibility,
     * window, duplicate, and seat checks before persisting.
     *
     * @param dto the registration request containing studentId and courseId
     * @return the created {@link RegistrationResponseDTO}
     * @throws RegistrationClosedException        if the registration window is closed for the semester
     * @throws InvalidCourseEligibilityException  if the student's academic profile is incomplete or mismatched
     * @throws CourseNotFoundException            if the course is not active
     * @throws DuplicateRegistrationException     if the student is already registered for this course in the semester
     * @throws CourseFullException                if no seats are available
     */
    @Override
    @Transactional
    public RegistrationResponseDTO registerCourse(RegistrationDTO dto) {
        Long studentId = dto.getStudentId();
        Long courseId = dto.getCourseId();

        logger.info("Registration attempt: studentId={}, courseId={}", studentId, courseId);

        // Fetch student and course
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> {
                    logger.warn("Registration failed: student not found with id={}", studentId);
                    return new ResourceNotFoundException("Student not found with id: " + studentId);
                });

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> {
                    logger.warn("Registration failed: course not found with id={}", courseId);
                    return new CourseNotFoundException("Course not found with id: " + courseId);
                });

        // Step 0 – Active course check
        if (!"ACTIVE".equalsIgnoreCase(course.getStatus())) {
            logger.warn("Registration failed: course id={} is not active (status={})", courseId, course.getStatus());
            throw new CourseNotFoundException("Course is not active.");
        }

        // Step 1 – Registration window check
        Semester courseSemester = course.getSemester();
        if (courseSemester == null) {
            logger.warn("Registration failed: course id={} has no semester assigned", courseId);
            throw new InvalidCourseEligibilityException("Student academic profile is incomplete. Please contact admin.");
        }

        boolean windowOpen = registrationWindowService.isWindowOpenForSemester(courseSemester.getId());
        if (!windowOpen) {
            logger.warn("Registration failed: window closed for semesterId={}", courseSemester.getId());
            throw new RegistrationClosedException("Registration window is currently closed for this semester.");
        }

        // Step 2 – Student eligibility (null-safety first)
        if (student.getBranch() == null || student.getMinorStream() == null || student.getCurrentSemester() == null
                || course.getBranch() == null || course.getMinorStream() == null) {
            logger.warn("Registration failed: incomplete academic profile for studentId={}", studentId);
            throw new InvalidCourseEligibilityException("Student academic profile is incomplete. Please contact admin.");
        }

        boolean branchMatch = student.getBranch().getId().equals(course.getBranch().getId());
        boolean streamMatch = student.getMinorStream().getId().equals(course.getMinorStream().getId());
        boolean semesterMatch = student.getCurrentSemester().getId().equals(courseSemester.getId());

        if (!branchMatch || !streamMatch || !semesterMatch) {
            String reason = !branchMatch ? "branch"
                    : !streamMatch ? "minor stream"
                    : "current semester";
            logger.warn("Registration failed: eligibility mismatch on {} for studentId={}, courseId={}", reason, studentId, courseId);
            throw new InvalidCourseEligibilityException(
                    "You are not eligible for this course. Mismatch in: " + reason + ".");
        }

        // Step 3 – Duplicate registration check & reactivation support
        java.util.Optional<Registration> existingReg = registrationRepository.findByStudentStudentIdAndCourseCourseIdAndSemesterId(
                studentId, courseId, courseSemester.getId());
        if (existingReg.isPresent() && "REGISTERED".equalsIgnoreCase(existingReg.get().getStatus())) {
            logger.warn("Registration failed: duplicate registration for studentId={}, courseId={}, semesterId={}",
                    studentId, courseId, courseSemester.getId());
            throw new DuplicateRegistrationException(
                    "Student is already registered for this course in the current semester.");
        }

        // Step 4 – Seat availability check
        if (course.getAvailableSeats() <= 0) {
            logger.warn("Registration failed: no available seats for courseId={}", courseId);
            throw new CourseFullException("No available seats for course: " + course.getCourseName());
        }

        // Step 5 – Create or reactivate registration
        Registration registration;
        if (existingReg.isPresent()) {
            registration = existingReg.get();
            registration.setStatus("REGISTERED");
            registration.setRegistrationDate(LocalDateTime.now());
        } else {
            registration = new Registration(student, course, courseSemester, LocalDateTime.now(), "REGISTERED");
        }
        registration = registrationRepository.save(registration);

        // Step 6 – Decrement available seats with optimistic locking guard
        course.setAvailableSeats(course.getAvailableSeats() - 1);
        try {
            courseRepository.save(course);
        } catch (org.springframework.orm.ObjectOptimisticLockingFailureException | jakarta.persistence.OptimisticLockException e) {
            logger.warn("Concurrent registration collision for courseId={}", courseId);
            throw new ConcurrentSeatUpdateException("Another student registered for this course simultaneously. Please try again.");
        }

        logger.info("Registration successful: registrationId={}, studentId={}, courseId={}",
                registration.getRegistrationId(), studentId, courseId);

        return mapToResponseDTO(registration);
    }

    /**
     * Retrieves a registration by its ID.
     *
     * @param id the registration ID
     * @return the matching {@link RegistrationResponseDTO}
     * @throws RegistrationNotFoundException if no registration exists with the given ID
     */
    @Override
    public RegistrationResponseDTO getRegistrationById(Long id) {
        Registration registration = registrationRepository.findById(id)
                .orElseThrow(() -> new RegistrationNotFoundException("Registration not found with id: " + id));
        return mapToResponseDTO(registration);
    }

    /**
     * Retrieves all registrations in the system.
     *
     * @return a list of all {@link RegistrationResponseDTO}s
     */
    @Override
    public List<RegistrationResponseDTO> getAllRegistrations() {
        return registrationRepository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a paginated list of all registrations.
     *
     * @param pageable pagination and sorting parameters
     * @return a {@link Page} of {@link RegistrationResponseDTO}s
     */
    @Override
    public Page<RegistrationResponseDTO> getRegistrationsPage(Pageable pageable) {
        return registrationRepository.findAll(pageable).map(this::mapToResponseDTO);
    }

    /**
     * Retrieves all registrations for a specific student.
     *
     * @param studentId the ID of the student
     * @return list of {@link RegistrationResponseDTO}s for the student
     */
    @Override
    public List<RegistrationResponseDTO> getStudentRegistrations(Long studentId) {
        return registrationRepository.findByStudentStudentId(studentId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all registrations for a specific course.
     *
     * @param courseId the ID of the course
     * @return list of {@link RegistrationResponseDTO}s for the course
     */
    @Override
    public List<RegistrationResponseDTO> getCourseRegistrations(Long courseId) {
        return registrationRepository.findByCourseCourseId(courseId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Admin operation to cancel a registration by ID.
     * <p>
     * If the registration was in REGISTERED status, the seat is returned to the course.
     * The registration record is then hard-deleted.
     * </p>
     *
     * @param id the registration ID to cancel
     * @throws ResourceNotFoundException if no registration exists with the given ID
     */
    @Override
    @Transactional
    public void cancelRegistration(Long id) {
        Registration registration = registrationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registration not found with id: " + id));

        if ("REGISTERED".equalsIgnoreCase(registration.getStatus())) {
            Course course = registration.getCourse();
            if (course != null) {
                int newSeats = course.getAvailableSeats() + 1;
                if (course.getMaxSeats() != null && newSeats > course.getMaxSeats()) {
                    newSeats = course.getMaxSeats();
                }
                course.setAvailableSeats(newSeats);
                courseRepository.save(course);
                logger.info("cancelRegistration: seat returned for courseId={}, availableSeats={}",
                        course.getCourseId(), newSeats);
            }
        }

        registration.setStatus("CANCELLED");
        registrationRepository.save(registration);
        logger.info("cancelRegistration: registration id={} cancelled by admin", id);
    }

    /**
     * Student-initiated drop of a registered course.
     * <p>
     * Validates ownership, deadline, and current status before returning the seat and updating status to DROPPED.
     * </p>
     *
     * @param registrationId the ID of the registration to drop
     * @param studentId      the ID of the student requesting the drop
     * @throws RegistrationNotFoundException  if no registration exists with the given ID
     * @throws UnauthorizedOperationException if the registration does not belong to the student
     * @throws IllegalStateException          if the registration is already dropped
     * @throws RegistrationClosedException   if the registration window is closed
     */
    @Override
    @Transactional
    public void dropCourse(Long registrationId, Long studentId) {
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> {
                    logger.warn("dropCourse failed: registration not found id={}", registrationId);
                    return new RegistrationNotFoundException("Registration not found with id: " + registrationId);
                });

        // Ownership verification
        if (!registration.getStudent().getStudentId().equals(studentId)) {
            logger.warn("dropCourse failed: studentId={} does not own registrationId={}", studentId, registrationId);
            throw new UnauthorizedOperationException(
                    "You are not authorized to drop this registration.");
        }

        // Status check
        if (!"REGISTERED".equalsIgnoreCase(registration.getStatus())) {
            logger.warn("dropCourse failed: registrationId={} is already in status={}", registrationId, registration.getStatus());
            throw new IllegalStateException("Course has already been dropped or is in an invalid state.");
        }

        // Deadline check: registration window must be currently open for the course's semester
        Semester courseSemester = registration.getCourse() != null ? registration.getCourse().getSemester() : registration.getSemester();
        if (courseSemester != null && !registrationWindowService.isWindowOpenForSemester(courseSemester.getId())) {
            logger.warn("dropCourse failed: registration window closed for semesterId={}", courseSemester.getId());
            throw new RegistrationClosedException("Cannot drop course: Registration window is currently closed for this semester.");
        }

        // Return seat (capped at maxSeats)
        Course course = registration.getCourse();
        if (course != null) {
            int newSeats = course.getAvailableSeats() + 1;
            if (course.getMaxSeats() != null && newSeats > course.getMaxSeats()) {
                newSeats = course.getMaxSeats();
            }
            course.setAvailableSeats(newSeats);
            try {
                courseRepository.save(course);
            } catch (org.springframework.orm.ObjectOptimisticLockingFailureException | jakarta.persistence.OptimisticLockException e) {
                logger.warn("Concurrent seat update collision on drop for courseId={}", course.getCourseId());
                throw new ConcurrentSeatUpdateException("Concurrent seat update conflict. Please try again.");
            }
            logger.info("dropCourse: seat returned for courseId={}, availableSeats={}",
                    course.getCourseId(), newSeats);
        }

        // Mark as DROPPED to preserve registration history
        registration.setStatus("DROPPED");
        registrationRepository.save(registration);
        logger.info("dropCourse: registrationId={} marked as DROPPED by studentId={}", registrationId, studentId);
    }

    /**
     * Maps a {@link Registration} entity to a {@link RegistrationResponseDTO},
     * populating all student, course, and semester fields with null-safe guards.
     *
     * @param reg the registration entity to map
     * @return the populated response DTO
     */
    private RegistrationResponseDTO mapToResponseDTO(Registration reg) {
        RegistrationResponseDTO dto = new RegistrationResponseDTO();

        dto.setRegistrationId(reg.getRegistrationId());
        dto.setRegistrationDate(reg.getRegistrationDate());
        dto.setStatus(reg.getStatus());

        Student s = reg.getStudent();
        if (s != null) {
            dto.setStudentId(s.getStudentId());
            dto.setStudentName(s.getName());
            dto.setStudentRollNo(s.getRollNo());
            dto.setStudentEmail(s.getEmail());
            dto.setStudentDepartment(s.getDepartment());
            dto.setBranchCode(s.getBranch() != null ? s.getBranch().getBranchCode() : null);
            dto.setMinorStreamName(s.getMinorStream() != null ? s.getMinorStream().getStreamName() : null);
            dto.setSectionName(s.getSection() != null ? s.getSection().getSectionName() : null);
        }

        Course c = reg.getCourse();
        if (c != null) {
            dto.setCourseId(c.getCourseId());
            dto.setCourseCode(c.getCourseCode());
            dto.setCourseName(c.getCourseName());
            dto.setCourseDepartment(c.getDepartment());
            dto.setCredits(c.getCredits());
            dto.setFacultyName(c.getFacultyName());
            dto.setDuration(c.getDuration());
        }

        // Semester number: prefer registration's own semester, fall back to course semester
        if (reg.getSemester() != null) {
            dto.setSemesterNumber(reg.getSemester().getSemesterNumber());
        } else if (c != null && c.getSemester() != null) {
            dto.setSemesterNumber(c.getSemester().getSemesterNumber());
        } else {
            dto.setSemesterNumber(null);
        }

        return dto;
    }
}
