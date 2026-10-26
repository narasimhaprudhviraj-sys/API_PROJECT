package com.example.onlinecourse.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class FacultyDTO {

    private Long id;

    @NotBlank(message = "Faculty code is required (e.g. FAC101)")
    private String facultyCode;

    @NotBlank(message = "Faculty name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @NotBlank(message = "Department is required")
    private String department;

    @NotBlank(message = "Designation is required")
    private String designation;

    private String phone;
    private boolean active = true;

    private int assignedSectionsCount;
    private int assignedCoursesCount;
    private int assignedStudentsCount;

    public FacultyDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFacultyCode() {
        return facultyCode;
    }

    public void setFacultyCode(String facultyCode) {
        this.facultyCode = facultyCode;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getAssignedSectionsCount() {
        return assignedSectionsCount;
    }

    public void setAssignedSectionsCount(int assignedSectionsCount) {
        this.assignedSectionsCount = assignedSectionsCount;
    }

    public int getAssignedCoursesCount() {
        return assignedCoursesCount;
    }

    public void setAssignedCoursesCount(int assignedCoursesCount) {
        this.assignedCoursesCount = assignedCoursesCount;
    }

    public int getAssignedStudentsCount() {
        return assignedStudentsCount;
    }

    public void setAssignedStudentsCount(int assignedStudentsCount) {
        this.assignedStudentsCount = assignedStudentsCount;
    }
}
