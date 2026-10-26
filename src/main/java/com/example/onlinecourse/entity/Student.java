package com.example.onlinecourse.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "student")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_id")
    private Long studentId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @JsonIgnore
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "roll_no", nullable = false, unique = true, length = 50)
    private String rollNo;

    @Column(name = "department", length = 50)
    private String department;

    @Column(name = "academic_year")
    private Integer year = 2; // Academic year (2, 3, 4)

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "branch_id")
    private Branch branch;

    /**
     * CORE REQUIREMENT: Fixed Minor Stream selected from second year onwards.
     * Cannot be changed by the student.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "minor_stream_id")
    private MinorStream minorStream;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "section_id")
    private Section section;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "semester_id")
    private Semester currentSemester;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "ACTIVE";

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Registration> registrations = new ArrayList<>();

    public Student() {
    }

    public Student(String name, String email, String password, String rollNo, String department, Integer year, String phone) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.rollNo = rollNo;
        this.department = department;
        this.year = year;
        this.phone = phone;
        this.status = "ACTIVE";
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRollNo() {
        return rollNo;
    }

    public void setRollNo(String rollNo) {
        this.rollNo = rollNo;
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

    public Section getSection() {
        return section;
    }

    public void setSection(Section section) {
        this.section = section;
    }

    public Semester getCurrentSemester() {
        return currentSemester;
    }

    public void setCurrentSemester(Semester currentSemester) {
        this.currentSemester = currentSemester;
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

    @Override
    public String toString() {
        return "Student{" +
                "studentId=" + studentId +
                ", name='" + name + '\'' +
                ", rollNo='" + rollNo + '\'' +
                ", branch=" + (branch != null ? branch.getBranchCode() : "N/A") +
                ", minorStream=" + (minorStream != null ? minorStream.getStreamName() : "N/A") +
                ", section=" + (section != null ? section.getSectionName() : "N/A") +
                ", semester=" + (currentSemester != null ? currentSemester.getSemesterNumber() : "N/A") +
                '}';
    }
}
