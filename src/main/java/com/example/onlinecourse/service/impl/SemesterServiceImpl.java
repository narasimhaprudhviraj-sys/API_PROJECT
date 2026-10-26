package com.example.onlinecourse.service.impl;

import com.example.onlinecourse.dto.SemesterDTO;
import com.example.onlinecourse.entity.Semester;
import com.example.onlinecourse.exception.SemesterNotFoundException;
import com.example.onlinecourse.repository.SemesterRepository;
import com.example.onlinecourse.service.SemesterService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SemesterServiceImpl implements SemesterService {

    private final SemesterRepository semesterRepository;

    public SemesterServiceImpl(SemesterRepository semesterRepository) {
        this.semesterRepository = semesterRepository;
    }

    @Override
    public List<SemesterDTO> getAllSemesters() {
        return semesterRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<SemesterDTO> getActiveSemesters() {
        return semesterRepository.findByActiveTrueOrderBySemesterNumberAsc().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public SemesterDTO getSemesterById(Long id) {
        Semester semester = semesterRepository.findById(id)
                .orElseThrow(() -> new SemesterNotFoundException("Semester not found with ID: " + id));
        return mapToDTO(semester);
    }

    @Override
    @Transactional
    public SemesterDTO createSemester(SemesterDTO dto) {
        if (semesterRepository.existsBySemesterNumber(dto.getSemesterNumber())) {
            throw new IllegalArgumentException("Semester number " + dto.getSemesterNumber() + " already exists");
        }
        Semester semester = new Semester(
                dto.getSemesterNumber(),
                dto.getAcademicYear() != null ? dto.getAcademicYear() : "2025-2026",
                dto.isActive()
        );
        Semester saved = semesterRepository.save(semester);
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public SemesterDTO updateSemester(Long id, SemesterDTO dto) {
        Semester semester = semesterRepository.findById(id)
                .orElseThrow(() -> new SemesterNotFoundException("Semester not found with ID: " + id));
        semester.setAcademicYear(dto.getAcademicYear());
        semester.setActive(dto.isActive());
        Semester updated = semesterRepository.save(semester);
        return mapToDTO(updated);
    }

    @Override
    @Transactional
    public void toggleSemesterStatus(Long id) {
        Semester semester = semesterRepository.findById(id)
                .orElseThrow(() -> new SemesterNotFoundException("Semester not found with ID: " + id));
        semester.setActive(!semester.isActive());
        semesterRepository.save(semester);
    }

    @Override
    @Transactional
    public void deleteSemester(Long id) {
        Semester semester = semesterRepository.findById(id)
                .orElseThrow(() -> new SemesterNotFoundException("Semester not found with ID: " + id));
        semesterRepository.delete(semester);
    }

    private SemesterDTO mapToDTO(Semester semester) {
        SemesterDTO dto = new SemesterDTO(
                semester.getId(),
                semester.getSemesterNumber(),
                semester.getAcademicYear(),
                semester.isActive()
        );
        if (semester.getCourses() != null) {
            dto.setCoursesCount(semester.getCourses().size());
        }
        return dto;
    }
}
