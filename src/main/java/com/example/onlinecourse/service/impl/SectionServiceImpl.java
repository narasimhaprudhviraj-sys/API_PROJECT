package com.example.onlinecourse.service.impl;

import com.example.onlinecourse.dto.SectionDTO;
import com.example.onlinecourse.entity.Branch;
import com.example.onlinecourse.entity.Faculty;
import com.example.onlinecourse.entity.MinorStream;
import com.example.onlinecourse.entity.Section;
import com.example.onlinecourse.exception.AcademicMismatchException;
import com.example.onlinecourse.exception.BranchNotFoundException;
import com.example.onlinecourse.exception.FacultyNotFoundException;
import com.example.onlinecourse.exception.MinorStreamNotFoundException;
import com.example.onlinecourse.exception.SectionNotFoundException;
import com.example.onlinecourse.repository.BranchRepository;
import com.example.onlinecourse.repository.FacultyRepository;
import com.example.onlinecourse.repository.MinorStreamRepository;
import com.example.onlinecourse.repository.SectionRepository;
import com.example.onlinecourse.service.SectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for Section management and Section-level faculty assignment.
 * Enforces RULES 6, 7 & 8:
 * - Faculty assignment is Section-level.
 * - All students in the section share the same faculty.
 * - When a minor stream has > 2 sections, Admin assigns designated faculty to each section.
 */
@Service
public class SectionServiceImpl implements SectionService {

    private static final Logger logger = LoggerFactory.getLogger(SectionServiceImpl.class);

    private final SectionRepository sectionRepository;
    private final BranchRepository branchRepository;
    private final MinorStreamRepository minorStreamRepository;
    private final FacultyRepository facultyRepository;

    public SectionServiceImpl(SectionRepository sectionRepository,
                              BranchRepository branchRepository,
                              MinorStreamRepository minorStreamRepository,
                              FacultyRepository facultyRepository) {
        this.sectionRepository = sectionRepository;
        this.branchRepository = branchRepository;
        this.minorStreamRepository = minorStreamRepository;
        this.facultyRepository = facultyRepository;
    }

    @Override
    public List<SectionDTO> getAllSections() {
        return sectionRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<SectionDTO> getSectionsByMinorStream(Long minorStreamId) {
        return sectionRepository.findByMinorStreamId(minorStreamId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<SectionDTO> getSectionsByFaculty(Long facultyId) {
        return sectionRepository.findByFacultyId(facultyId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public SectionDTO getSectionById(Long id) {
        Section section = sectionRepository.findById(id)
                .orElseThrow(() -> new SectionNotFoundException("Section not found with ID: " + id));
        return mapToDTO(section);
    }

    @Override
    @Transactional
    public SectionDTO createSection(SectionDTO dto) {
        logger.info("Admin creating section: {} for minor stream ID: {}", dto.getSectionName(), dto.getMinorStreamId());

        Branch branch = branchRepository.findById(dto.getBranchId())
                .orElseThrow(() -> new BranchNotFoundException("Branch not found with ID: " + dto.getBranchId()));

        MinorStream minorStream = minorStreamRepository.findById(dto.getMinorStreamId())
                .orElseThrow(() -> new MinorStreamNotFoundException("Minor stream not found with ID: " + dto.getMinorStreamId()));

        if (minorStream.getBranch() != null && !minorStream.getBranch().getId().equals(branch.getId())) {
            throw new AcademicMismatchException("Minor Stream '" + minorStream.getStreamName() + "' does not belong to Branch '" + branch.getBranchName() + "'.");
        }

        Faculty faculty = null;
        if (dto.getFacultyId() != null) {
            faculty = facultyRepository.findById(dto.getFacultyId())
                    .orElseThrow(() -> new FacultyNotFoundException("Faculty not found with ID: " + dto.getFacultyId()));
            long existingSections = sectionRepository.countByMinorStreamId(minorStream.getId());
            if (existingSections <= 2) {
                throw new AcademicMismatchException("Faculty selection is enabled only after a minor stream has more than two sections. Create the section first, then assign faculty.");
            }
            validateFacultyBranch(branch, faculty);
        }

        Section section = new Section(
                dto.getSectionName().trim(),
                branch,
                minorStream,
                dto.getAcademicYear() != null ? dto.getAcademicYear() : "2025-2026",
                faculty,
                dto.isActive()
        );

        Section saved = sectionRepository.save(section);
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public SectionDTO updateSection(Long id, SectionDTO dto) {
        Section section = sectionRepository.findById(id)
                .orElseThrow(() -> new SectionNotFoundException("Section not found with ID: " + id));

        section.setSectionName(dto.getSectionName().trim());
        section.setAcademicYear(dto.getAcademicYear());
        section.setActive(dto.isActive());

        if (dto.getBranchId() != null) {
            Branch branch = branchRepository.findById(dto.getBranchId())
                    .orElseThrow(() -> new BranchNotFoundException("Branch not found with ID: " + dto.getBranchId()));
            section.setBranch(branch);
        }

        if (dto.getMinorStreamId() != null) {
            MinorStream minorStream = minorStreamRepository.findById(dto.getMinorStreamId())
                    .orElseThrow(() -> new MinorStreamNotFoundException("Minor stream not found with ID: " + dto.getMinorStreamId()));
            section.setMinorStream(minorStream);
        }

        if (section.getBranch() != null && section.getMinorStream() != null && section.getMinorStream().getBranch() != null) {
            if (!section.getMinorStream().getBranch().getId().equals(section.getBranch().getId())) {
                throw new AcademicMismatchException("Minor Stream '" + section.getMinorStream().getStreamName() + "' does not belong to Branch '" + section.getBranch().getBranchName() + "'.");
            }
        }

        if (section.getFaculty() != null) {
            validateFacultyBranch(section.getBranch(), section.getFaculty());
        }

        if (dto.getFacultyId() != null) {
            Faculty faculty = facultyRepository.findById(dto.getFacultyId())
                    .orElseThrow(() -> new FacultyNotFoundException("Faculty not found with ID: " + dto.getFacultyId()));
            long sectionCount = sectionRepository.countByMinorStreamId(section.getMinorStream().getId());
            if (sectionCount <= 2) {
                throw new AcademicMismatchException("Faculty selection is enabled only when a minor stream has more than two sections. Current sections: " + sectionCount);
            }
            validateFacultyBranch(section.getBranch(), faculty);
            section.setFaculty(faculty);
        } else {
            section.setFaculty(null);
        }

        Section updated = sectionRepository.save(section);
        return mapToDTO(updated);
    }

    @Override
    @Transactional
    public SectionDTO assignFacultyToSection(Long sectionId, Long facultyId) {
        logger.info("Admin assigning Faculty ID: {} to Section ID: {}", facultyId, sectionId);

        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new SectionNotFoundException("Section not found with ID: " + sectionId));

        Faculty faculty = facultyRepository.findById(facultyId)
                .orElseThrow(() -> new FacultyNotFoundException("Faculty not found with ID: " + facultyId));

        validateFacultyBranch(section.getBranch(), faculty);
        long sectionCount = sectionRepository.countByMinorStreamId(section.getMinorStream().getId());
        if (sectionCount <= 2) {
            throw new AcademicMismatchException("Faculty selection is enabled only when a minor stream has more than two sections. Current sections: " + sectionCount);
        }

        section.setFaculty(faculty);
        Section saved = sectionRepository.save(section);
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public void deleteSection(Long id) {
        if (!sectionRepository.existsById(id)) {
            throw new SectionNotFoundException("Section not found with ID: " + id);
        }
        sectionRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void toggleSectionStatus(Long id) {
        Section section = sectionRepository.findById(id)
                .orElseThrow(() -> new SectionNotFoundException("Section not found with ID: " + id));
        section.setActive(!section.isActive());
        sectionRepository.save(section);
    }

    private void validateFacultyBranch(Branch branch, Faculty faculty) {
        if (branch != null && faculty != null && faculty.getDepartment() != null
                && !branch.getBranchCode().equalsIgnoreCase(faculty.getDepartment().trim())) {
            throw new AcademicMismatchException("Faculty " + faculty.getName() + " belongs to department "
                    + faculty.getDepartment() + " and cannot be assigned to branch " + branch.getBranchCode() + ".");
        }
    }

    private SectionDTO mapToDTO(Section section) {
        SectionDTO dto = new SectionDTO();
        dto.setId(section.getId());
        dto.setSectionName(section.getSectionName());
        dto.setAcademicYear(section.getAcademicYear());
        dto.setActive(section.isActive());

        if (section.getBranch() != null) {
            dto.setBranchId(section.getBranch().getId());
            dto.setBranchCode(section.getBranch().getBranchCode());
        }

        if (section.getMinorStream() != null) {
            dto.setMinorStreamId(section.getMinorStream().getId());
            dto.setMinorStreamName(section.getMinorStream().getStreamName());
        }

        if (section.getFaculty() != null) {
            dto.setFacultyId(section.getFaculty().getId());
            dto.setFacultyName(section.getFaculty().getName());
        }

        if (section.getStudents() != null) {
            dto.setStudentsCount(section.getStudents().size());
        }

        if (section.getMinorStream() != null) {
            int count = (int) sectionRepository.countByMinorStreamId(section.getMinorStream().getId());
            dto.setSectionsInMinorStream(count);
            dto.setFacultyAssignmentEnabled(count > 2);
        }

        return dto;
    }
}
