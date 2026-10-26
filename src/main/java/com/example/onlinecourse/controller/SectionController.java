package com.example.onlinecourse.controller;

import com.example.onlinecourse.dto.SectionDTO;
import com.example.onlinecourse.service.SectionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing sections within the Online Course Registration System.
 */
@RestController
@RequestMapping("/api/sections")
@CrossOrigin(origins = "*")
public class SectionController {

    private final SectionService sectionService;

    public SectionController(SectionService sectionService) {
        this.sectionService = sectionService;
    }

    @GetMapping
    public ResponseEntity<List<SectionDTO>> getAllSections() {
        return ResponseEntity.ok(sectionService.getAllSections());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SectionDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(sectionService.getSectionById(id));
    }

    @GetMapping("/minor-stream/{minorStreamId}")
    public ResponseEntity<List<SectionDTO>> getByMinorStream(@PathVariable Long minorStreamId) {
        return ResponseEntity.ok(sectionService.getSectionsByMinorStream(minorStreamId));
    }

    @PostMapping
    public ResponseEntity<SectionDTO> createSection(@Valid @RequestBody SectionDTO sectionDTO) {
        SectionDTO created = sectionService.createSection(sectionDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SectionDTO> updateSection(@PathVariable Long id,
                                                     @Valid @RequestBody SectionDTO sectionDTO) {
        return ResponseEntity.ok(sectionService.updateSection(id, sectionDTO));
    }

    @PutMapping("/{id}/assign-faculty/{facultyId}")
    public ResponseEntity<SectionDTO> assignFaculty(@PathVariable Long id,
                                                     @PathVariable Long facultyId) {
        return ResponseEntity.ok(sectionService.assignFacultyToSection(id, facultyId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSection(@PathVariable Long id) {
        sectionService.deleteSection(id);
        return ResponseEntity.noContent().build();
    }
}
