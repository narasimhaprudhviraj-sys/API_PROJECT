package com.example.onlinecourse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class MinorStreamDTO {

    private Long id;

    @NotBlank(message = "Minor stream code is required")
    @Size(min = 2, max = 30, message = "Code must be between 2 and 30 characters")
    private String streamCode;

    @NotBlank(message = "Minor stream name is required")
    @Size(min = 3, max = 150, message = "Name must be between 3 and 150 characters")
    private String streamName;

    @NotNull(message = "Associated branch ID is required")
    private Long branchId;

    private String branchCode;
    private String branchName;
    private boolean active = true;
    private int sectionsCount;
    private int coursesCount;

    public MinorStreamDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStreamCode() {
        return streamCode;
    }

    public void setStreamCode(String streamCode) {
        this.streamCode = streamCode;
    }

    public String getStreamName() {
        return streamName;
    }

    public void setStreamName(String streamName) {
        this.streamName = streamName;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getSectionsCount() {
        return sectionsCount;
    }

    public void setSectionsCount(int sectionsCount) {
        this.sectionsCount = sectionsCount;
    }

    public int getCoursesCount() {
        return coursesCount;
    }

    public void setCoursesCount(int coursesCount) {
        this.coursesCount = coursesCount;
    }
}
