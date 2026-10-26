package com.example.onlinecourse.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class StudentResponseDTO {

    private Long studentId;
    private String name;
    private String email;
    private String rollNo;
    private String department;
    private Integer year;
    private String phone;

    private Long branchId;
    private String branchCode;
    private String branchName;

    // Fixed Minor Stream (cannot be altered by student)
    private Long minorStreamId;
    private String minorStreamName;

    private Long sectionId;
    private String sectionName;

    private Long semesterId;
    private Integer semesterNumber;

    // Faculty assigned at Section level
    private String assignedFacultyName;

    private String status = "ACTIVE";
    private int registeredCoursesCount;

    public StudentResponseDTO() {
    }

    public StudentResponseDTO(Long studentId, String name, String email, String rollNo, String department, Integer year, String phone) {
        this.studentId = studentId;
        this.name = name;
        this.email = email;
        this.rollNo = rollNo;
        this.department = department;
        this.year = year;
        this.phone = phone;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRollNo() {
        return rollNo;
    }

    public void setRollNo(String rollNo) {
        this.rollNo = rollNo;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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

    public Long getSectionId() {
        return sectionId;
    }

    public void setSectionId(Long sectionId) {
        this.sectionId = sectionId;
    }

    public String getSectionName() {
        return sectionName;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }

    public Long getSemesterId() {
        return semesterId;
    }

    public void setSemesterId(Long semesterId) {
        this.semesterId = semesterId;
    }

    public Long getCurrentSemesterId() {
        return semesterId;
    }

    public void setCurrentSemesterId(Long semesterId) {
        this.semesterId = semesterId;
    }

    public Integer getSemesterNumber() {
        return semesterNumber;
    }

    public void setSemesterNumber(Integer semesterNumber) {
        this.semesterNumber = semesterNumber;
    }

    public Integer getCurrentSemesterNumber() {
        return semesterNumber;
    }

    public void setCurrentSemesterNumber(Integer semesterNumber) {
        this.semesterNumber = semesterNumber;
    }

    public String getAssignedFacultyName() {
        return assignedFacultyName;
    }

    public void setAssignedFacultyName(String assignedFacultyName) {
        this.assignedFacultyName = assignedFacultyName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getRegisteredCoursesCount() {
        return registeredCoursesCount;
    }

    public void setRegisteredCoursesCount(int registeredCoursesCount) {
        this.registeredCoursesCount = registeredCoursesCount;
    }
}
