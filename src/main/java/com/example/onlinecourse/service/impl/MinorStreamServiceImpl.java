package com.example.onlinecourse.service.impl;

import com.example.onlinecourse.dto.MinorStreamDTO;
import com.example.onlinecourse.entity.Branch;
import com.example.onlinecourse.entity.MinorStream;
import com.example.onlinecourse.exception.BranchNotFoundException;
import com.example.onlinecourse.exception.MinorStreamNotFoundException;
import com.example.onlinecourse.repository.BranchRepository;
import com.example.onlinecourse.repository.MinorStreamRepository;
import com.example.onlinecourse.service.MinorStreamService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for Minor Stream management.
 * Demonstrates Constructor Injection (CO1) and Transactional operations.
 */
@Service
public class MinorStreamServiceImpl implements MinorStreamService {

    private static final Logger logger = LoggerFactory.getLogger(MinorStreamServiceImpl.class);

    private final MinorStreamRepository minorStreamRepository;
    private final BranchRepository branchRepository;

    public MinorStreamServiceImpl(MinorStreamRepository minorStreamRepository,
                                  BranchRepository branchRepository) {
        this.minorStreamRepository = minorStreamRepository;
        this.branchRepository = branchRepository;
    }

    @Override
    public List<MinorStreamDTO> getAllMinorStreams() {
        return minorStreamRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<MinorStreamDTO> getMinorStreamsByBranch(Long branchId) {
        return minorStreamRepository.findByBranchId(branchId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<MinorStreamDTO> getActiveMinorStreamsByBranch(Long branchId) {
        return minorStreamRepository.findByBranchIdAndActiveTrue(branchId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public MinorStreamDTO getMinorStreamById(Long id) {
        MinorStream stream = minorStreamRepository.findById(id)
                .orElseThrow(() -> new MinorStreamNotFoundException("Minor stream not found with ID: " + id));
        return mapToDTO(stream);
    }

    @Override
    @Transactional
    public MinorStreamDTO createMinorStream(MinorStreamDTO dto) {
        logger.info("Admin creating minor stream: {} under branch ID: {}", dto.getStreamName(), dto.getBranchId());

        if (minorStreamRepository.existsByStreamCodeIgnoreCase(dto.getStreamCode())) {
            throw new IllegalArgumentException("Minor stream code already exists: " + dto.getStreamCode());
        }

        Branch branch = branchRepository.findById(dto.getBranchId())
                .orElseThrow(() -> new BranchNotFoundException("Branch not found with ID: " + dto.getBranchId()));

        MinorStream stream = new MinorStream(
                dto.getStreamCode().trim(),
                dto.getStreamName().trim(),
                branch,
                dto.isActive()
        );

        MinorStream saved = minorStreamRepository.save(stream);
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public MinorStreamDTO updateMinorStream(Long id, MinorStreamDTO dto) {
        MinorStream stream = minorStreamRepository.findById(id)
                .orElseThrow(() -> new MinorStreamNotFoundException("Minor stream not found with ID: " + id));

        if (dto.getBranchId() != null && !dto.getBranchId().equals(stream.getBranch().getId())) {
            Branch branch = branchRepository.findById(dto.getBranchId())
                    .orElseThrow(() -> new BranchNotFoundException("Branch not found with ID: " + dto.getBranchId()));
            stream.setBranch(branch);
        }

        stream.setStreamName(dto.getStreamName().trim());
        stream.setActive(dto.isActive());

        MinorStream updated = minorStreamRepository.save(stream);
        return mapToDTO(updated);
    }

    @Override
    @Transactional
    public void deleteMinorStream(Long id) {
        if (!minorStreamRepository.existsById(id)) {
            throw new MinorStreamNotFoundException("Minor stream not found with ID: " + id);
        }
        minorStreamRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void toggleMinorStreamStatus(Long id) {
        MinorStream stream = minorStreamRepository.findById(id)
                .orElseThrow(() -> new MinorStreamNotFoundException("Minor stream not found with ID: " + id));
        stream.setActive(!stream.isActive());
        minorStreamRepository.save(stream);
    }

    private MinorStreamDTO mapToDTO(MinorStream stream) {
        MinorStreamDTO dto = new MinorStreamDTO();
        dto.setId(stream.getId());
        dto.setStreamCode(stream.getStreamCode());
        dto.setStreamName(stream.getStreamName());
        dto.setActive(stream.isActive());
        if (stream.getBranch() != null) {
            dto.setBranchId(stream.getBranch().getId());
            dto.setBranchCode(stream.getBranch().getBranchCode());
            dto.setBranchName(stream.getBranch().getBranchName());
        }
        if (stream.getSections() != null) {
            dto.setSectionsCount(stream.getSections().size());
        }
        if (stream.getCourses() != null) {
            dto.setCoursesCount(stream.getCourses().size());
        }
        return dto;
    }
}
