package com.example.onlinecourse.dto;

import jakarta.validation.constraints.*;

public class CourseDTO {

    private Long courseId;

    @NotBlank(message = "Course code is required")
    @Size(min = 2, max = 50, message = "Course code must be between 2 and 50 characters")
    private String courseCode;

    @NotBlank(message = "Course name is required")
    @Size(min = 3, max = 150, message = "Course name must be between 3 and 150 characters")
    private String courseName;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    private String department;

    private Long branchId;
    private String branchCode;
    private String branchName;

    // Course mapped to specific minor stream
    private Long minorStreamId;
    private String minorStreamName;

    // Course mapped to specific semester
    private Long semesterId;
    private Integer semesterNumber;

    // Assigned Faculty
    private Long facultyId;
    private String facultyName;

    @NotNull(message = "Credits are required")
    @Min(value = 1, message = "Credits must be at least 1")
    @Max(value = 10, message = "Credits cannot exceed 10")
    private Integer credits;

    @NotNull(message = "Maximum seats is required")
    @Min(value = 1, message = "Maximum seats must be at least 1")
    private Integer maxSeats;

    private Integer availableSeats;

    private String duration = "1 Semester";
    private String status = "ACTIVE";
    private boolean registered; // Used by student eligible courses view

    public CourseDTO() {
    }

    public CourseDTO(Long courseId, String courseCode, String courseName, String description,
                     String department, Integer credits, String facultyName, Integer maxSeats,
                     Integer availableSeats, String duration, String status) {
        this.courseId = courseId;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.description = description;
        this.department = department;
        this.credits = credits;
        this.facultyName = facultyName;
        this.maxSeats = maxSeats;
        this.availableSeats = availableSeats;
        this.duration = duration;
        this.status = status != null ? status : "ACTIVE";
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
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

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
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

    public Long getSemesterId() {
        return semesterId;
    }

    public void setSemesterId(Long semesterId) {
        this.semesterId = semesterId;
    }

    public Integer getSemesterNumber() {
        return semesterNumber;
    }

    public void setSemesterNumber(Integer semesterNumber) {
        this.semesterNumber = semesterNumber;
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

    public Integer getCredits() {
        return credits;
    }

    public void setCredits(Integer credits) {
        this.credits = credits;
    }

    public Integer getMaxSeats() {
        return maxSeats;
    }

    public void setMaxSeats(Integer maxSeats) {
        this.maxSeats = maxSeats;
    }

    public Integer getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(Integer availableSeats) {
        this.availableSeats = availableSeats;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isRegistered() {
        return registered;
    }

    public void setRegistered(boolean registered) {
        this.registered = registered;
    }
}
