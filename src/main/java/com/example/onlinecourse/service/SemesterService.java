package com.example.onlinecourse.service;

import com.example.onlinecourse.dto.SemesterDTO;
import java.util.List;

public interface SemesterService {
    List<SemesterDTO> getAllSemesters();
    List<SemesterDTO> getActiveSemesters();
    SemesterDTO getSemesterById(Long id);
    SemesterDTO createSemester(SemesterDTO dto);
    SemesterDTO updateSemester(Long id, SemesterDTO dto);
    void toggleSemesterStatus(Long id);
    void deleteSemester(Long id);
}
