package com.example.onlinecourse.service;

import com.example.onlinecourse.dto.SectionDTO;
import java.util.List;

public interface SectionService {
    List<SectionDTO> getAllSections();
    List<SectionDTO> getSectionsByMinorStream(Long minorStreamId);
    List<SectionDTO> getSectionsByFaculty(Long facultyId);
    SectionDTO getSectionById(Long id);
    SectionDTO createSection(SectionDTO dto);
    SectionDTO updateSection(Long id, SectionDTO dto);
    SectionDTO assignFacultyToSection(Long sectionId, Long facultyId);
    void deleteSection(Long id);
    void toggleSectionStatus(Long id);
}
