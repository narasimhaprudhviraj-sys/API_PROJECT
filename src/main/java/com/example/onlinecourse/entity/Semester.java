package com.example.onlinecourse.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "semester")
public class Semester {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "semester_id")
    private Long id;

    @Column(name = "semester_number", nullable = false)
    private Integer semesterNumber;

    @Column(name = "academic_year", nullable = false, length = 30)
    private String academicYear = "2025-2026";

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @JsonIgnore
    @OneToMany(mappedBy = "semester")
    private List<Course> courses = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "semester")
    private List<RegistrationWindow> registrationWindows = new ArrayList<>();

    public Semester() {
    }

    public Semester(Integer semesterNumber, String academicYear, boolean active) {
        this.semesterNumber = semesterNumber;
        this.academicYear = academicYear;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getSemesterNumber() {
        return semesterNumber;
    }

    public void setSemesterNumber(Integer semesterNumber) {
        this.semesterNumber = semesterNumber;
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

    public List<Course> getCourses() {
        return courses;
    }

    public void setCourses(List<Course> courses) {
        this.courses = courses;
    }

    public List<RegistrationWindow> getRegistrationWindows() {
        return registrationWindows;
    }

    public void setRegistrationWindows(List<RegistrationWindow> registrationWindows) {
        this.registrationWindows = registrationWindows;
    }
}
