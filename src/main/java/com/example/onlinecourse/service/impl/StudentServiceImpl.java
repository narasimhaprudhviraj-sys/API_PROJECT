package com.example.onlinecourse.service.impl;

import com.example.onlinecourse.config.JwtService;
import com.example.onlinecourse.dto.LoginRequest;
import com.example.onlinecourse.dto.LoginResponse;
import com.example.onlinecourse.dto.StudentDTO;
import com.example.onlinecourse.dto.StudentResponseDTO;
import com.example.onlinecourse.entity.Branch;
import com.example.onlinecourse.entity.MinorStream;
import com.example.onlinecourse.entity.Section;
import com.example.onlinecourse.entity.Semester;
import com.example.onlinecourse.entity.Student;
import com.example.onlinecourse.exception.AcademicMismatchException;
import com.example.onlinecourse.exception.BranchNotFoundException;
import com.example.onlinecourse.exception.DuplicateRegistrationException;
import com.example.onlinecourse.exception.InvalidCredentialsException;
import com.example.onlinecourse.exception.MinorStreamNotFoundException;
import com.example.onlinecourse.exception.SectionNotFoundException;
import com.example.onlinecourse.exception.SemesterNotFoundException;
import com.example.onlinecourse.exception.StudentNotFoundException;
import com.example.onlinecourse.exception.UnauthorizedOperationException;
import com.example.onlinecourse.repository.BranchRepository;
import com.example.onlinecourse.repository.MinorStreamRepository;
import com.example.onlinecourse.repository.SectionRepository;
import com.example.onlinecourse.repository.SemesterRepository;
import com.example.onlinecourse.repository.StudentRepository;
import com.example.onlinecourse.service.StudentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for Student management, authentication via JWT,
 * and academic lifecycle operations.
 */
@Service
public class StudentServiceImpl implements StudentService {

    private static final Logger logger = LoggerFactory.getLogger(StudentServiceImpl.class);

    private final StudentRepository studentRepository;
    private final BranchRepository branchRepository;
    private final MinorStreamRepository minorStreamRepository;
    private final SectionRepository sectionRepository;
    private final SemesterRepository semesterRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public StudentServiceImpl(StudentRepository studentRepository,
                              BranchRepository branchRepository,
                              MinorStreamRepository minorStreamRepository,
                              SectionRepository sectionRepository,
                              SemesterRepository semesterRepository,
                              BCryptPasswordEncoder passwordEncoder,
                              JwtService jwtService) {
        this.studentRepository = studentRepository;
        this.branchRepository = branchRepository;
        this.minorStreamRepository = minorStreamRepository;
        this.sectionRepository = sectionRepository;
        this.semesterRepository = semesterRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    @Transactional
    public StudentResponseDTO registerStudent(StudentDTO dto) {
        logger.info("Registering new student: {} with roll number: {}", dto.getName(), dto.getRollNo());

        if (studentRepository.existsByEmail(dto.getEmail().trim())) {
            throw new DuplicateRegistrationException("Student with email '" + dto.getEmail() + "' already exists.");
        }
        if (studentRepository.existsByRollNo(dto.getRollNo().trim())) {
            throw new DuplicateRegistrationException("Student with roll number '" + dto.getRollNo() + "' already exists.");
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

        Section section = null;
        if (dto.getSectionId() != null) {
            section = sectionRepository.findById(dto.getSectionId())
                    .orElseThrow(() -> new SectionNotFoundException("Section not found with ID: " + dto.getSectionId()));
        }

        Semester semester = null;
        if (dto.getSemesterId() != null) {
            semester = semesterRepository.findById(dto.getSemesterId())
                    .orElseThrow(() -> new SemesterNotFoundException("Semester not found with ID: " + dto.getSemesterId()));
        }

        // Validate academic hierarchy consistency
        validateAcademicConsistency(branch, minorStream, section);

        String rawPassword = (dto.getPassword() != null && !dto.getPassword().isBlank())
                ? dto.getPassword() : "student123";

        Student student = new Student();
        student.setName(dto.getName().trim());
        student.setEmail(dto.getEmail().trim().toLowerCase());
        student.setPassword(passwordEncoder.encode(rawPassword));
        student.setRollNo(dto.getRollNo().trim().toUpperCase());
        student.setYear(dto.getYear() != null ? dto.getYear() : 2);
        student.setPhone(dto.getPhone() != null ? dto.getPhone().trim() : "");
        student.setBranch(branch);
        student.setMinorStream(minorStream);
        student.setSection(section);
        student.setCurrentSemester(semester);
        student.setStatus("ACTIVE");

        Student saved = studentRepository.save(student);
        return mapToResponseDTO(saved);
    }

    @Override
    @Transactional
    public LoginResponse loginStudent(LoginRequest loginRequest) {
        String email = loginRequest.getEmail().trim().toLowerCase();
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid student email or password."));

        boolean matches = false;
        if (student.getPassword().startsWith("$2a$") || student.getPassword().startsWith("$2b$")) {
            matches = passwordEncoder.matches(loginRequest.getPassword(), student.getPassword());
        } else {
            matches = student.getPassword().equals(loginRequest.getPassword());
            if (matches) {
                student.setPassword(passwordEncoder.encode(loginRequest.getPassword()));
                studentRepository.save(student);
            }
        }

        if (!matches) {
            throw new InvalidCredentialsException("Invalid student email or password.");
        }

        String jwtToken = jwtService.generateToken(student.getEmail(), student.getStudentId(), "ROLE_STUDENT", student.getName());
        LoginResponse response = new LoginResponse(
                true,
                "Student login successful",
                "STUDENT",
                student.getStudentId(),
                student.getName(),
                student.getEmail(),
                jwtToken
        );
        response.setRollNo(student.getRollNo());

        if (student.getBranch() != null) {
            response.setBranchCode(student.getBranch().getBranchCode());
        }
        if (student.getMinorStream() != null) {
            response.setMinorStreamName(student.getMinorStream().getStreamName());
        }
        if (student.getSection() != null) {
            response.setSectionName(student.getSection().getSectionName());
        }
        if (student.getCurrentSemester() != null) {
            response.setSemesterNumber(student.getCurrentSemester().getSemesterNumber());
        }

        logger.info("Student {} ({}) logged in successfully with JWT", student.getName(), student.getRollNo());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public StudentResponseDTO getStudentById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new StudentNotFoundException("Student not found with ID: " + id));
        return mapToResponseDTO(student);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentResponseDTO> getAllStudents() {
        return studentRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StudentResponseDTO> getStudentsPage(Pageable pageable) {
        return studentRepository.findAll(pageable).map(this::mapToResponseDTO);
    }

    @Override
    @Transactional
    public StudentResponseDTO updateStudent(Long id, StudentDTO dto) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new StudentNotFoundException("Student not found with ID: " + id));

        Branch branch = student.getBranch();
        if (dto.getBranchId() != null) {
            branch = branchRepository.findById(dto.getBranchId())
                    .orElseThrow(() -> new BranchNotFoundException("Branch not found with ID: " + dto.getBranchId()));
        }

        MinorStream minorStream = student.getMinorStream();
        if (dto.getMinorStreamId() != null) {
            minorStream = minorStreamRepository.findById(dto.getMinorStreamId())
                    .orElseThrow(() -> new MinorStreamNotFoundException("Minor stream not found with ID: " + dto.getMinorStreamId()));
        }

        Section section = student.getSection();
        if (dto.getSectionId() != null) {
            section = sectionRepository.findById(dto.getSectionId())
                    .orElseThrow(() -> new SectionNotFoundException("Section not found with ID: " + dto.getSectionId()));
        }

        Semester semester = student.getCurrentSemester();
        if (dto.getSemesterId() != null) {
            semester = semesterRepository.findById(dto.getSemesterId())
                    .orElseThrow(() -> new SemesterNotFoundException("Semester not found with ID: " + dto.getSemesterId()));
        }

        validateAcademicConsistency(branch, minorStream, section);

        student.setName(dto.getName().trim());
        student.setEmail(dto.getEmail().trim().toLowerCase());
        student.setRollNo(dto.getRollNo().trim().toUpperCase());
        student.setYear(dto.getYear());
        student.setPhone(dto.getPhone() != null ? dto.getPhone().trim() : "");
        student.setBranch(branch);
        student.setMinorStream(minorStream);
        student.setSection(section);
        student.setCurrentSemester(semester);

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            student.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        Student updated = studentRepository.save(student);
        return mapToResponseDTO(updated);
    }

    /**
     * When student edits their own profile, they CANNOT alter branch, minorStream, section, or semester.
     * Enforces RULE: Minor stream and branch remain fixed and cannot be changed by the student.
     */
    @Override
    @Transactional
    public StudentResponseDTO updateStudentSelfProfile(Long id, StudentDTO dto) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new StudentNotFoundException("Student not found with ID: " + id));

        // Prevent student modification of fixed minor stream
        if (dto.getMinorStreamId() != null && student.getMinorStream() != null &&
                !dto.getMinorStreamId().equals(student.getMinorStream().getId())) {
            throw new UnauthorizedOperationException("Policy Violation: Minor stream is fixed and cannot be altered by student.");
        }

        // Prevent student modification of branch
        if (dto.getBranchId() != null && student.getBranch() != null &&
                !dto.getBranchId().equals(student.getBranch().getId())) {
            throw new UnauthorizedOperationException("Policy Violation: Branch cannot be altered by student.");
        }

        // Prevent student modification of section
        if (dto.getSectionId() != null && student.getSection() != null &&
                !dto.getSectionId().equals(student.getSection().getId())) {
            throw new UnauthorizedOperationException("Policy Violation: Section assignment cannot be altered by student.");
        }

        if (dto.getName() != null && !dto.getName().isBlank()) {
            student.setName(dto.getName().trim());
        }
        if (dto.getPhone() != null) {
            student.setPhone(dto.getPhone().trim());
        }

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            student.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        Student updated = studentRepository.save(student);
        return mapToResponseDTO(updated);
    }

    @Override
    @Transactional
    public void deleteStudent(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new StudentNotFoundException("Student not found with ID: " + id));
        studentRepository.delete(student);
        logger.info("Admin deleted student ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentResponseDTO> searchStudents(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllStudents();
        }
        String cleanKeyword = keyword.trim();
        return studentRepository.findByNameContainingIgnoreCaseOrRollNoContainingIgnoreCase(
                        cleanKeyword, cleanKeyword,
                        org.springframework.data.domain.PageRequest.of(0, 200)
                ).getContent().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public StudentResponseDTO assignStudentAcademics(Long studentId, Long branchId, Long minorStreamId, Long sectionId, Long semesterId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException("Student not found with ID: " + studentId));

        Branch branch = student.getBranch();
        if (branchId != null) {
            branch = branchRepository.findById(branchId)
                    .orElseThrow(() -> new BranchNotFoundException("Branch not found with ID: " + branchId));
        }

        MinorStream minorStream = student.getMinorStream();
        if (minorStreamId != null) {
            minorStream = minorStreamRepository.findById(minorStreamId)
                    .orElseThrow(() -> new MinorStreamNotFoundException("Minor stream not found with ID: " + minorStreamId));
        }

        Section section = student.getSection();
        if (sectionId != null) {
            section = sectionRepository.findById(sectionId)
                    .orElseThrow(() -> new SectionNotFoundException("Section not found with ID: " + sectionId));
        }

        Semester semester = student.getCurrentSemester();
        if (semesterId != null) {
            semester = semesterRepository.findById(semesterId)
                    .orElseThrow(() -> new SemesterNotFoundException("Semester not found with ID: " + semesterId));
        }

        validateAcademicConsistency(branch, minorStream, section);

        student.setBranch(branch);
        student.setMinorStream(minorStream);
        student.setSection(section);
        student.setCurrentSemester(semester);

        Student updated = studentRepository.save(student);
        logger.info("Admin assigned academics to student {}: branch={}, minorStream={}, section={}, semester={}",
                student.getStudentId(),
                branch != null ? branch.getBranchCode() : null,
                minorStream != null ? minorStream.getStreamName() : null,
                section != null ? section.getSectionName() : null,
                semester != null ? semester.getSemesterNumber() : null);

        return mapToResponseDTO(updated);
    }

    private void validateAcademicConsistency(Branch branch, MinorStream minorStream, Section section) {
        if (branch != null && minorStream != null && minorStream.getBranch() != null &&
                !minorStream.getBranch().getId().equals(branch.getId())) {
            throw new AcademicMismatchException("Minor stream '" + minorStream.getStreamName() + "' does not belong to branch '" + branch.getBranchCode() + "'.");
        }

        if (section != null) {
            if (branch != null && section.getBranch() != null && !section.getBranch().getId().equals(branch.getId())) {
                throw new AcademicMismatchException("Section '" + section.getSectionName() + "' does not belong to branch '" + branch.getBranchCode() + "'.");
            }
            if (minorStream != null && section.getMinorStream() != null && !section.getMinorStream().getId().equals(minorStream.getId())) {
                throw new AcademicMismatchException("Section '" + section.getSectionName() + "' does not belong to minor stream '" + minorStream.getStreamName() + "'.");
            }
        }
    }

    private StudentResponseDTO mapToResponseDTO(Student student) {
        StudentResponseDTO dto = new StudentResponseDTO();
        dto.setStudentId(student.getStudentId());
        dto.setName(student.getName());
        dto.setEmail(student.getEmail());
        dto.setRollNo(student.getRollNo());
        dto.setDepartment(student.getDepartment());
        dto.setYear(student.getYear());
        dto.setPhone(student.getPhone());
        dto.setStatus(student.getStatus());

        if (student.getBranch() != null) {
            dto.setBranchId(student.getBranch().getId());
            dto.setBranchCode(student.getBranch().getBranchCode());
            dto.setBranchName(student.getBranch().getBranchName());
        }

        if (student.getMinorStream() != null) {
            dto.setMinorStreamId(student.getMinorStream().getId());
            dto.setMinorStreamName(student.getMinorStream().getStreamName());
        }

        if (student.getSection() != null) {
            dto.setSectionId(student.getSection().getId());
            dto.setSectionName(student.getSection().getSectionName());

            if (student.getSection().getFaculty() != null) {
                dto.setAssignedFacultyName(student.getSection().getFaculty().getName());
            }
        }

        if (student.getCurrentSemester() != null) {
            dto.setCurrentSemesterId(student.getCurrentSemester().getId());
            dto.setCurrentSemesterNumber(student.getCurrentSemester().getSemesterNumber());
        }

        if (student.getRegistrations() != null) {
            dto.setRegisteredCoursesCount((int) student.getRegistrations().stream()
                    .filter(r -> "REGISTERED".equalsIgnoreCase(r.getStatus()))
                    .count());
        }

        return dto;
    }
}
