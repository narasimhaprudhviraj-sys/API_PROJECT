package com.example.onlinecourse.service;

import com.example.onlinecourse.dto.BranchDTO;
import java.util.List;

public interface BranchService {
    List<BranchDTO> getAllBranches();
    List<BranchDTO> getActiveBranches();
    BranchDTO getBranchById(Long id);
    BranchDTO createBranch(BranchDTO branchDTO);
    BranchDTO updateBranch(Long id, BranchDTO branchDTO);
    void deleteBranch(Long id);
    void toggleBranchStatus(Long id);
}
