package com.example.onlinecourse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class SectionDTO {

    private Long id;

    @NotBlank(message = "Section name is required (e.g. MAD Stream-1)")
    @Size(min = 2, max = 100, message = "Section name must be between 2 and 100 characters")
    private String sectionName;

    @NotNull(message = "Branch ID is required")
    private Long branchId;
    private String branchCode;

    @NotNull(message = "Minor Stream ID is required")
    private Long minorStreamId;
    private String minorStreamName;

    private Long facultyId;
    private String facultyName;

    private String academicYear = "2025-2026";
    private boolean active = true;
    private int studentsCount;
    private int sectionsInMinorStream;
    private boolean facultyAssignmentEnabled;

    public SectionDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSectionName() {
        return sectionName;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }

    public Long getBranchId() {
        return branchId;
    }

    public void setBranchId(Long branchId) {
        this.branchId = branchId;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }

    public Long getMinorStreamId() {
        return minorStreamId;
    }

    public void setMinorStreamId(Long minorStreamId) {
        this.minorStreamId = minorStreamId;
    }

    public String getMinorStreamName() {
        return minorStreamName;
    }

    public void setMinorStreamName(String minorStreamName) {
        this.minorStreamName = minorStreamName;
    }

    public Long getFacultyId() {
        return facultyId;
    }

    public void setFacultyId(Long facultyId) {
        this.facultyId = facultyId;
    }

    public String getFacultyName() {
        return facultyName;
    }

    public void setFacultyName(String facultyName) {
        this.facultyName = facultyName;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getStudentsCount() {
        return studentsCount;
    }

    public void setStudentsCount(int studentsCount) {
        this.studentsCount = studentsCount;
    }

    public int getSectionsInMinorStream() {
        return sectionsInMinorStream;
    }

    public void setSectionsInMinorStream(int sectionsInMinorStream) {
        this.sectionsInMinorStream = sectionsInMinorStream;
    }

    public boolean isFacultyAssignmentEnabled() {
        return facultyAssignmentEnabled;
    }

    public void setFacultyAssignmentEnabled(boolean facultyAssignmentEnabled) {
        this.facultyAssignmentEnabled = facultyAssignmentEnabled;
    }
}
