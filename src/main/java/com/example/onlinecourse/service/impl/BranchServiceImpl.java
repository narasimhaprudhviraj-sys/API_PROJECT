package com.example.onlinecourse.service.impl;

import com.example.onlinecourse.dto.BranchDTO;
import com.example.onlinecourse.entity.Branch;
import com.example.onlinecourse.exception.BranchNotFoundException;
import com.example.onlinecourse.repository.BranchRepository;
import com.example.onlinecourse.service.BranchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for Branch administration.
 * Demonstrates Constructor Injection (CO1).
 */
@Service
public class BranchServiceImpl implements BranchService {

    private static final Logger logger = LoggerFactory.getLogger(BranchServiceImpl.class);

    private final BranchRepository branchRepository;

    // Constructor Injection
    public BranchServiceImpl(BranchRepository branchRepository) {
        this.branchRepository = branchRepository;
    }

    @Override
    public List<BranchDTO> getAllBranches() {
        return branchRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<BranchDTO> getActiveBranches() {
        return branchRepository.findByActiveTrue().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public BranchDTO getBranchById(Long id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new BranchNotFoundException("Branch not found with ID: " + id));
        return mapToDTO(branch);
    }

    @Override
    @Transactional
    public BranchDTO createBranch(BranchDTO branchDTO) {
        logger.info("Admin creating new branch: {}", branchDTO.getBranchCode());
        if (branchRepository.existsByBranchCodeIgnoreCase(branchDTO.getBranchCode())) {
            throw new IllegalArgumentException("Branch code already exists: " + branchDTO.getBranchCode());
        }

        Branch branch = new Branch(
                branchDTO.getBranchCode().toUpperCase().trim(),
                branchDTO.getBranchName().trim(),
                branchDTO.isActive()
        );

        Branch saved = branchRepository.save(branch);
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public BranchDTO updateBranch(Long id, BranchDTO branchDTO) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new BranchNotFoundException("Branch not found with ID: " + id));

        branch.setBranchName(branchDTO.getBranchName().trim());
        branch.setActive(branchDTO.isActive());
        Branch updated = branchRepository.save(branch);
        return mapToDTO(updated);
    }

    @Override
    @Transactional
    public void deleteBranch(Long id) {
        if (!branchRepository.existsById(id)) {
            throw new BranchNotFoundException("Branch not found with ID: " + id);
        }
        branchRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void toggleBranchStatus(Long id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new BranchNotFoundException("Branch not found with ID: " + id));
        branch.setActive(!branch.isActive());
        branchRepository.save(branch);
    }

    private BranchDTO mapToDTO(Branch branch) {
        return new BranchDTO(branch.getId(), branch.getBranchCode(), branch.getBranchName(), branch.isActive());
    }
}
