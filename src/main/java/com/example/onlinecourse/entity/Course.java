package com.example.onlinecourse.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "course")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_id")
    private Long courseId;

    @Column(name = "course_code", nullable = false, unique = true, length = 50)
    private String courseCode;

    @Column(name = "course_name", nullable = false, length = 150)
    private String courseName;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "department", length = 50)
    private String department;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "branch_id")
    private Branch branch;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "minor_stream_id")
    private MinorStream minorStream;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "semester_id")
    private Semester semester;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "faculty_id")
    private Faculty faculty;

    @Column(name = "faculty_name", length = 100)
    private String facultyName;

    @Column(name = "credits", nullable = false)
    private Integer credits;

    @Column(name = "max_seats", nullable = false)
    private Integer maxSeats;

    @Column(name = "available_seats", nullable = false)
    private Integer availableSeats;

    @Column(name = "duration", length = 50)
    private String duration = "1 Semester";

    @Column(name = "status", nullable = false, length = 20)
    private String status = "ACTIVE"; // "ACTIVE", "INACTIVE"

    @Version
    @Column(name = "version")
    private Long version;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Registration> registrations = new ArrayList<>();

    public Course() {
    }

    public Course(String courseCode, String courseName, String description, Branch branch,
                  MinorStream minorStream, Semester semester, Faculty faculty,
                  Integer credits, Integer maxSeats, Integer availableSeats,
                  String duration, String status) {
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.description = description;
        this.branch = branch;
        this.minorStream = minorStream;
        this.semester = semester;
        this.faculty = faculty;
        this.credits = credits;
        this.maxSeats = maxSeats;
        this.availableSeats = availableSeats;
        this.duration = duration != null ? duration : "1 Semester";
        this.status = status != null ? status : "ACTIVE";
        if (branch != null) {
            this.department = branch.getBranchCode();
        }
        if (faculty != null) {
            this.facultyName = faculty.getName();
        }
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
        if (department == null && branch != null) {
            return branch.getBranchCode();
        }
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Branch getBranch() {
        return branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
        if (branch != null) {
            this.department = branch.getBranchCode();
        }
    }

    public MinorStream getMinorStream() {
        return minorStream;
    }

    public void setMinorStream(MinorStream minorStream) {
        this.minorStream = minorStream;
    }

    public Semester getSemester() {
        return semester;
    }

    public void setSemester(Semester semester) {
        this.semester = semester;
    }

    public Faculty getFaculty() {
        return faculty;
    }

    public void setFaculty(Faculty faculty) {
        this.faculty = faculty;
        if (faculty != null) {
            this.facultyName = faculty.getName();
        }
    }

    public Integer getCredits() {
        return credits;
    }

    public void setCredits(Integer credits) {
        this.credits = credits;
    }

    public String getFacultyName() {
        if (facultyName == null && faculty != null) {
            return faculty.getName();
        }
        return facultyName;
    }

    public void setFacultyName(String facultyName) {
        this.facultyName = facultyName;
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

    public List<Registration> getRegistrations() {
        return registrations;
    }

    public void setRegistrations(List<Registration> registrations) {
        this.registrations = registrations;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "Course{" +
                "courseId=" + courseId +
                ", courseCode='" + courseCode + '\'' +
                ", courseName='" + courseName + '\'' +
                ", credits=" + credits +
                ", maxSeats=" + maxSeats +
                ", availableSeats=" + availableSeats +
                ", status='" + status + '\'' +
                '}';
    }
}
