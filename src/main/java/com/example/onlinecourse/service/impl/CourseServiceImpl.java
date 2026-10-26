package com.example.onlinecourse.service.impl;

import com.example.onlinecourse.dto.CourseDTO;
import com.example.onlinecourse.entity.*;
import com.example.onlinecourse.exception.*;
import com.example.onlinecourse.repository.*;
import com.example.onlinecourse.service.CourseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for Course management and Course Eligibility calculations.
 * Enforces RULES 9, 10, 13, 14:
 * - Course eligibility is strictly backend checked based on Branch + Fixed Minor Stream + Semester.
 * - Prevents registration for inactive or full courses.
 */
@Service
public class CourseServiceImpl implements CourseService {

    private static final Logger logger = LoggerFactory.getLogger(CourseServiceImpl.class);

    private final CourseRepository courseRepository;
    private final BranchRepository branchRepository;
    private final MinorStreamRepository minorStreamRepository;
    private final SemesterRepository semesterRepository;
    private final FacultyRepository facultyRepository;
    private final StudentRepository studentRepository;
    private final RegistrationRepository registrationRepository;

    public CourseServiceImpl(CourseRepository courseRepository,
                             BranchRepository branchRepository,
                             MinorStreamRepository minorStreamRepository,
                             SemesterRepository semesterRepository,
                             FacultyRepository facultyRepository,
                             StudentRepository studentRepository,
                             RegistrationRepository registrationRepository) {
        this.courseRepository = courseRepository;
        this.branchRepository = branchRepository;
        this.minorStreamRepository = minorStreamRepository;
        this.semesterRepository = semesterRepository;
        this.facultyRepository = facultyRepository;
        this.studentRepository = studentRepository;
        this.registrationRepository = registrationRepository;
    }

    @Override
    @Transactional
    public CourseDTO addCourse(CourseDTO dto) {
        String cleanCode = dto.getCourseCode().trim().toUpperCase();
        if (courseRepository.existsByCourseCode(cleanCode)) {
            throw new DuplicateRegistrationException("Course with code '" + cleanCode + "' already exists.");
        }

        Branch branch = null;
        if (dto.getBranchId() != null) {
            branch = branchRepository.findById(dto.getBranchId())
                    .orElseThrow(() -> new BranchNotFoundException("Branch not found with ID: " + dto.getBranchId()));
        }

        MinorStream minorStream = null;
        if (dto.getMinorStreamId() != null) {
            minorStream = minorStreamRepository.findById(dto.getMinorStreamId())
                    .orElseThrow(() -> new MinorStreamNotFoundException("Minor stream not found with ID: " + dto.getMinorStreamId()));
            if (branch == null && minorStream.getBranch() != null) {
                branch = minorStream.getBranch();
            }
        }

        Semester semester = null;
        if (dto.getSemesterId() != null) {
            semester = semesterRepository.findById(dto.getSemesterId())
                    .orElseThrow(() -> new SemesterNotFoundException("Semester not found with ID: " + dto.getSemesterId()));
        }

        Faculty faculty = null;
        if (dto.getFacultyId() != null) {
            faculty = facultyRepository.findById(dto.getFacultyId())
                    .orElseThrow(() -> new FacultyNotFoundException("Faculty not found with ID: " + dto.getFacultyId()));
        }

        if (branch != null && faculty != null && faculty.getDepartment() != null
                && !branch.getBranchCode().equalsIgnoreCase(faculty.getDepartment().trim())) {
            throw new AcademicMismatchException("Faculty " + faculty.getName() + " belongs to department "
                    + faculty.getDepartment() + " and cannot teach a " + branch.getBranchCode() + " course.");
        }

        if (branch != null && minorStream != null && minorStream.getBranch() != null) {
            if (!branch.getId().equals(minorStream.getBranch().getId())) {
                throw new AcademicMismatchException("Minor Stream '" + minorStream.getStreamName() + "' does not belong to Branch '" + branch.getBranchName() + "'.");
            }
        }

        Course course = new Course();
        course.setCourseCode(cleanCode);
        course.setCourseName(dto.getCourseName().trim());
        course.setDescription(dto.getDescription() != null ? dto.getDescription().trim() : "");
        course.setCredits(dto.getCredits());
        course.setMaxSeats(dto.getMaxSeats());

        int availableSeats = (dto.getAvailableSeats() != null && dto.getAvailableSeats() >= 0 && dto.getAvailableSeats() <= dto.getMaxSeats())
                ? dto.getAvailableSeats()
                : dto.getMaxSeats();
        course.setAvailableSeats(availableSeats);

        course.setBranch(branch);
        course.setMinorStream(minorStream);
        course.setSemester(semester);
        course.setFaculty(faculty);
        course.setDuration(dto.getDuration() != null ? dto.getDuration().trim() : "1 Semester");
        course.setStatus(dto.getStatus() != null && !dto.getStatus().isBlank() ? dto.getStatus().trim().toUpperCase() : "ACTIVE");

        if (branch != null) {
            course.setDepartment(branch.getBranchCode());
        }
        if (faculty != null) {
            course.setFacultyName(faculty.getName());
        } else if (dto.getFacultyName() != null) {
            course.setFacultyName(dto.getFacultyName().trim());
        }

        Course saved = courseRepository.save(course);
        logger.info("Admin created new course: {} - {}", saved.getCourseCode(), saved.getCourseName());
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public CourseDTO updateCourse(Long id, CourseDTO dto) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new CourseNotFoundException("Course not found with ID: " + id));

        String cleanCode = dto.getCourseCode().trim().toUpperCase();
        if (!course.getCourseCode().equalsIgnoreCase(cleanCode) && courseRepository.existsByCourseCode(cleanCode)) {
            throw new DuplicateRegistrationException("Course code '" + cleanCode + "' already exists in another course.");
        }

        course.setCourseCode(cleanCode);
        course.setCourseName(dto.getCourseName().trim());
        course.setDescription(dto.getDescription() != null ? dto.getDescription().trim() : "");
        course.setCredits(dto.getCredits());

        if (dto.getBranchId() != null) {
            Branch branch = branchRepository.findById(dto.getBranchId())
                    .orElseThrow(() -> new BranchNotFoundException("Branch not found with ID: " + dto.getBranchId()));
            course.setBranch(branch);
            course.setDepartment(branch.getBranchCode());
        }

        if (dto.getMinorStreamId() != null) {
            MinorStream minorStream = minorStreamRepository.findById(dto.getMinorStreamId())
                    .orElseThrow(() -> new MinorStreamNotFoundException("Minor stream not found with ID: " + dto.getMinorStreamId()));
            course.setMinorStream(minorStream);
        }

        if (dto.getSemesterId() != null) {
            Semester semester = semesterRepository.findById(dto.getSemesterId())
                    .orElseThrow(() -> new SemesterNotFoundException("Semester not found with ID: " + dto.getSemesterId()));
            course.setSemester(semester);
        }

        if (dto.getFacultyId() != null) {
            Faculty faculty = facultyRepository.findById(dto.getFacultyId())
                    .orElseThrow(() -> new FacultyNotFoundException("Faculty not found with ID: " + dto.getFacultyId()));
            course.setFaculty(faculty);
            course.setFacultyName(faculty.getName());
        }

        Branch currentBranch = course.getBranch();
        MinorStream currentMinorStream = course.getMinorStream();
        if (currentBranch != null && currentMinorStream != null && currentMinorStream.getBranch() != null) {
            if (!currentBranch.getId().equals(currentMinorStream.getBranch().getId())) {
                throw new AcademicMismatchException("Minor Stream '" + currentMinorStream.getStreamName() + "' does not belong to Branch '" + currentBranch.getBranchName() + "'.");
            }
        }

        if (currentBranch != null && course.getFaculty() != null && course.getFaculty().getDepartment() != null
                && !currentBranch.getBranchCode().equalsIgnoreCase(course.getFaculty().getDepartment().trim())) {
            throw new AcademicMismatchException("Faculty " + course.getFaculty().getName() + " belongs to department "
                    + course.getFaculty().getDepartment() + " and cannot teach a " + currentBranch.getBranchCode() + " course.");
        }

        int oldMax = course.getMaxSeats();
        int newMax = dto.getMaxSeats();
        course.setMaxSeats(newMax);

        if (dto.getAvailableSeats() != null) {
            int newAvailable = Math.min(dto.getAvailableSeats(), newMax);
            course.setAvailableSeats(Math.max(0, newAvailable));
        } else {
            int diff = newMax - oldMax;
            int adjusted = course.getAvailableSeats() + diff;
            course.setAvailableSeats(Math.max(0, Math.min(adjusted, newMax)));
        }

        if (dto.getDuration() != null) course.setDuration(dto.getDuration().trim());
        if (dto.getStatus() != null && !dto.getStatus().isBlank()) course.setStatus(dto.getStatus().trim().toUpperCase());

        Course updated = courseRepository.save(course);
        return mapToDTO(updated);
    }

    @Override
    @Transactional
    public void deleteCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new CourseNotFoundException("Course not found with ID: " + id));
        courseRepository.delete(course);
        logger.info("Admin deleted course with ID: {}", id);
    }

    @Override
    @Transactional
    public void toggleCourseStatus(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new CourseNotFoundException("Course not found with ID: " + id));
        if ("ACTIVE".equalsIgnoreCase(course.getStatus())) {
            course.setStatus("INACTIVE");
        } else {
            course.setStatus("ACTIVE");
        }
        courseRepository.save(course);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseDTO getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new CourseNotFoundException("Course not found with ID: " + id));
        return mapToDTO(course);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseDTO> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseDTO> getCoursesPage(Pageable pageable) {
        return courseRepository.findAll(pageable).map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseDTO> searchCourses(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllCourses();
        }
        return courseRepository.findByCourseNameContainingIgnoreCase(keyword.trim()).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseDTO> filterCoursesByDepartment(String department) {
        if (department == null || department.trim().equalsIgnoreCase("ALL")) {
            return getAllCourses();
        }
        return courseRepository.findByDepartmentIgnoreCase(department.trim()).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseDTO> getAvailableCourses() {
        return courseRepository.findByAvailableSeatsGreaterThan(0).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseDTO> filterCourses(String keyword, String department, boolean onlyAvailable) {
        String cleanKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        String cleanDepartment = (department != null && !department.trim().isEmpty() && !department.equalsIgnoreCase("ALL")) ? department.trim() : null;

        return courseRepository.filterCourses(cleanKeyword, cleanDepartment, onlyAvailable).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * CORE REQUIREMENT:
     * Backend determines course eligibility strictly based on:
     * 1. Student Branch
     * 2. Student Fixed Minor Stream
     * 3. Student Current Semester
     * 4. Active course status
     * Also checks and flags whether the student has already registered for each course.
     */
    @Override
    @Transactional(readOnly = true)
    public List<CourseDTO> getEligibleCoursesForStudent(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException("Student not found with ID: " + studentId));

        if (student.getBranch() == null || student.getMinorStream() == null || student.getCurrentSemester() == null) {
            logger.warn("Student {} lacks branch/minor stream/semester assignment", student.getRollNo());
            return Collections.emptyList();
        }

        Long branchId = student.getBranch().getId();
        Long minorStreamId = student.getMinorStream().getId();
        Long semesterId = student.getCurrentSemester().getId();

        List<Course> eligibleCourses = courseRepository.findEligibleCourses(branchId, minorStreamId, semesterId);

        return eligibleCourses.stream().map(course -> {
            CourseDTO dto = mapToDTO(course);
            boolean isRegistered = registrationRepository.isStudentRegisteredForCourseInSemester(
                    studentId, course.getCourseId(), semesterId);
            dto.setRegistered(isRegistered);
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<CourseDTO> getCoursesByMinorStream(Long minorStreamId) {
        return courseRepository.findByMinorStreamId(minorStreamId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CourseDTO> getCoursesBySemester(Long semesterId) {
        return courseRepository.findBySemesterId(semesterId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CourseDTO> getCoursesByFaculty(Long facultyId) {
        return courseRepository.findByFacultyId(facultyId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private CourseDTO mapToDTO(Course course) {
        CourseDTO dto = new CourseDTO();
        dto.setCourseId(course.getCourseId());
        dto.setCourseCode(course.getCourseCode());
        dto.setCourseName(course.getCourseName());
        dto.setDescription(course.getDescription());
        dto.setCredits(course.getCredits());
        dto.setMaxSeats(course.getMaxSeats());
        dto.setAvailableSeats(course.getAvailableSeats());
        dto.setDuration(course.getDuration());
        dto.setStatus(course.getStatus());

        if (course.getBranch() != null) {
            dto.setBranchId(course.getBranch().getId());
            dto.setBranchCode(course.getBranch().getBranchCode());
            dto.setBranchName(course.getBranch().getBranchName());
            dto.setDepartment(course.getBranch().getBranchCode());
        } else {
            dto.setDepartment(course.getDepartment());
        }

        if (course.getMinorStream() != null) {
            dto.setMinorStreamId(course.getMinorStream().getId());
            dto.setMinorStreamName(course.getMinorStream().getStreamName());
        }

        if (course.getSemester() != null) {
            dto.setSemesterId(course.getSemester().getId());
            dto.setSemesterNumber(course.getSemester().getSemesterNumber());
        }

        if (course.getFaculty() != null) {
            dto.setFacultyId(course.getFaculty().getId());
            dto.setFacultyName(course.getFaculty().getName());
        } else {
            dto.setFacultyName(course.getFacultyName());
        }

        return dto;
    }
}
