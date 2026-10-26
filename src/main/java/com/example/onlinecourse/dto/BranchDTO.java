package com.example.onlinecourse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class BranchDTO {

    private Long id;

    @NotBlank(message = "Branch code is required")
    @Size(min = 2, max = 20, message = "Branch code must be between 2 and 20 characters")
    private String branchCode;

    @NotBlank(message = "Branch name is required")
    @Size(min = 3, max = 150, message = "Branch name must be between 3 and 150 characters")
    private String branchName;

    private boolean active = true;

    public BranchDTO() {
    }

    public BranchDTO(Long id, String branchCode, String branchName, boolean active) {
        this.id = id;
        this.branchCode = branchCode;
        this.branchName = branchName;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
}
