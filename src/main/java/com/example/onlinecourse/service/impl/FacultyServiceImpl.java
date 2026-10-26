package com.example.onlinecourse.service.impl;

import com.example.onlinecourse.dto.CourseDTO;
import com.example.onlinecourse.dto.FacultyDTO;
import com.example.onlinecourse.dto.SectionDTO;
import com.example.onlinecourse.dto.StudentResponseDTO;
import com.example.onlinecourse.entity.Course;
import com.example.onlinecourse.entity.Faculty;
import com.example.onlinecourse.entity.Section;
import com.example.onlinecourse.entity.Student;
import com.example.onlinecourse.exception.FacultyNotFoundException;
import com.example.onlinecourse.repository.CourseRepository;
import com.example.onlinecourse.repository.FacultyRepository;
import com.example.onlinecourse.repository.SectionRepository;
import com.example.onlinecourse.repository.StudentRepository;
import com.example.onlinecourse.service.FacultyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for Faculty management and Faculty Dashboard.
 * Enforces RULES 18 & 19: Faculty can only access assigned sections and students.
 */
@Service
public class FacultyServiceImpl implements FacultyService {

    private static final Logger logger = LoggerFactory.getLogger(FacultyServiceImpl.class);

    private final FacultyRepository facultyRepository;
    private final SectionRepository sectionRepository;
    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    public FacultyServiceImpl(FacultyRepository facultyRepository,
                              SectionRepository sectionRepository,
                              CourseRepository courseRepository,
                              StudentRepository studentRepository,
                              PasswordEncoder passwordEncoder) {
        this.facultyRepository = facultyRepository;
        this.sectionRepository = sectionRepository;
        this.courseRepository = courseRepository;
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<FacultyDTO> getAllFaculty() {
        return facultyRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<FacultyDTO> getFacultyPage(Pageable pageable) {
        return facultyRepository.findAll(pageable).map(this::mapToDTO);
    }

    @Override
    public FacultyDTO getFacultyById(Long id) {
        Faculty faculty = facultyRepository.findById(id)
                .orElseThrow(() -> new FacultyNotFoundException("Faculty not found with ID: " + id));
        return mapToDTO(faculty);
    }

    @Override
    @Transactional
    public FacultyDTO createFaculty(FacultyDTO dto) {
        logger.info("Admin creating faculty member: {} ({})", dto.getName(), dto.getFacultyCode());

        if (facultyRepository.existsByEmailIgnoreCase(dto.getEmail())) {
            throw new IllegalArgumentException("Faculty email already in use: " + dto.getEmail());
        }
        if (facultyRepository.existsByFacultyCodeIgnoreCase(dto.getFacultyCode())) {
            throw new IllegalArgumentException("Faculty code already in use: " + dto.getFacultyCode());
        }

        String rawPassword = (dto.getPassword() != null && !dto.getPassword().isBlank())
                ? dto.getPassword() : "faculty123";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        Faculty faculty = new Faculty(
                dto.getFacultyCode().toUpperCase().trim(),
                dto.getName().trim(),
                dto.getEmail().toLowerCase().trim(),
                encodedPassword,
                dto.getDepartment().trim(),
                dto.getDesignation().trim(),
                dto.getPhone(),
                dto.isActive()
        );

        Faculty saved = facultyRepository.save(faculty);
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public FacultyDTO updateFaculty(Long id, FacultyDTO dto) {
        Faculty faculty = facultyRepository.findById(id)
                .orElseThrow(() -> new FacultyNotFoundException("Faculty not found with ID: " + id));

        faculty.setName(dto.getName().trim());
        faculty.setDepartment(dto.getDepartment().trim());
        faculty.setDesignation(dto.getDesignation().trim());
        faculty.setPhone(dto.getPhone());
        faculty.setActive(dto.isActive());

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            faculty.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        Faculty updated = facultyRepository.save(faculty);
        return mapToDTO(updated);
    }

    @Override
    @Transactional
    public void deleteFaculty(Long id) {
        if (!facultyRepository.existsById(id)) {
            throw new FacultyNotFoundException("Faculty not found with ID: " + id);
        }
        facultyRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void toggleFacultyStatus(Long id) {
        Faculty faculty = facultyRepository.findById(id)
                .orElseThrow(() -> new FacultyNotFoundException("Faculty not found with ID: " + id));
        faculty.setActive(!faculty.isActive());
        facultyRepository.save(faculty);
    }

    @Override
    public List<SectionDTO> getAssignedSections(Long facultyId) {
        if (!facultyRepository.existsById(facultyId)) {
            throw new FacultyNotFoundException("Faculty not found with ID: " + facultyId);
        }
        return sectionRepository.findByFacultyId(facultyId).stream()
                .map(this::mapSectionToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CourseDTO> getAssignedCourses(Long facultyId) {
        if (!facultyRepository.existsById(facultyId)) {
            throw new FacultyNotFoundException("Faculty not found with ID: " + facultyId);
        }
        return courseRepository.findByFacultyId(facultyId).stream()
                .map(this::mapCourseToDTO)
                .collect(Collectors.toList());
    }

    /**
     * CORE REQUIREMENT: Faculty can ONLY access students in their assigned sections!
     * Protects unrelated student privacy.
     */
    @Override
    public List<StudentResponseDTO> getAssignedStudents(Long facultyId) {
        if (!facultyRepository.existsById(facultyId)) {
            throw new FacultyNotFoundException("Faculty not found with ID: " + facultyId);
        }
        List<Student> students = studentRepository.findStudentsByAssignedFacultyId(facultyId);
        return students.stream()
                .map(this::mapStudentToDTO)
                .collect(Collectors.toList());
    }

    private FacultyDTO mapToDTO(Faculty faculty) {
        FacultyDTO dto = new FacultyDTO();
        dto.setId(faculty.getId());
        dto.setFacultyCode(faculty.getFacultyCode());
        dto.setName(faculty.getName());
        dto.setEmail(faculty.getEmail());
        dto.setDepartment(faculty.getDepartment());
        dto.setDesignation(faculty.getDesignation());
        dto.setPhone(faculty.getPhone());
        dto.setActive(faculty.isActive());
        if (faculty.getSections() != null) {
            dto.setAssignedSectionsCount(faculty.getSections().size());
        }
        if (faculty.getCourses() != null) {
            dto.setAssignedCoursesCount(faculty.getCourses().size());
        }
        return dto;
    }

    private SectionDTO mapSectionToDTO(Section section) {
        SectionDTO dto = new SectionDTO();
        dto.setId(section.getId());
        dto.setSectionName(section.getSectionName());
        dto.setAcademicYear(section.getAcademicYear());
        dto.setActive(section.isActive());
        if (section.getBranch() != null) {
            dto.setBranchCode(section.getBranch().getBranchCode());
        }
        if (section.getMinorStream() != null) {
            dto.setMinorStreamName(section.getMinorStream().getStreamName());
        }
        if (section.getStudents() != null) {
            dto.setStudentsCount(section.getStudents().size());
        }
        return dto;
    }

    private CourseDTO mapCourseToDTO(Course course) {
        CourseDTO dto = new CourseDTO();
        dto.setCourseId(course.getCourseId());
        dto.setCourseCode(course.getCourseCode());
        dto.setCourseName(course.getCourseName());
        dto.setCredits(course.getCredits());
        dto.setMaxSeats(course.getMaxSeats());
        dto.setAvailableSeats(course.getAvailableSeats());
        dto.setStatus(course.getStatus());
        if (course.getBranch() != null) {
            dto.setBranchCode(course.getBranch().getBranchCode());
        }
        if (course.getMinorStream() != null) {
            dto.setMinorStreamName(course.getMinorStream().getStreamName());
        }
        if (course.getSemester() != null) {
            dto.setSemesterNumber(course.getSemester().getSemesterNumber());
        }
        return dto;
    }

    private StudentResponseDTO mapStudentToDTO(Student student) {
        StudentResponseDTO dto = new StudentResponseDTO();
        dto.setStudentId(student.getStudentId());
        dto.setName(student.getName());
        dto.setEmail(student.getEmail());
        dto.setRollNo(student.getRollNo());
        dto.setYear(student.getYear());
        dto.setPhone(student.getPhone());
        dto.setStatus(student.getStatus());
        if (student.getBranch() != null) {
            dto.setBranchCode(student.getBranch().getBranchCode());
            dto.setBranchName(student.getBranch().getBranchName());
        }
        if (student.getMinorStream() != null) {
            dto.setMinorStreamName(student.getMinorStream().getStreamName());
        }
        if (student.getSection() != null) {
            dto.setSectionName(student.getSection().getSectionName());
            if (student.getSection().getFaculty() != null) {
                dto.setAssignedFacultyName(student.getSection().getFaculty().getName());
            }
        }
        if (student.getCurrentSemester() != null) {
            dto.setSemesterNumber(student.getCurrentSemester().getSemesterNumber());
        }
        if (student.getRegistrations() != null) {
            dto.setRegisteredCoursesCount((int) student.getRegistrations().stream()
                    .filter(r -> "REGISTERED".equalsIgnoreCase(r.getStatus())).count());
        }
        return dto;
    }
}
