package com.example.onlinecourse.service.impl;

import com.example.onlinecourse.dto.RegistrationWindowDTO;
import com.example.onlinecourse.entity.RegistrationWindow;
import com.example.onlinecourse.entity.Semester;
import com.example.onlinecourse.exception.InvalidRegistrationWindowException;
import com.example.onlinecourse.exception.ResourceNotFoundException;
import com.example.onlinecourse.exception.SemesterNotFoundException;
import com.example.onlinecourse.repository.RegistrationWindowRepository;
import com.example.onlinecourse.repository.SemesterRepository;
import com.example.onlinecourse.service.RegistrationWindowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service implementation for Registration Window and Deadline management.
 * Enforces RULE 11 & 12: Backend verifies window deadline on every registration attempt.
 */
@Service
public class RegistrationWindowServiceImpl implements RegistrationWindowService {

    private static final Logger logger = LoggerFactory.getLogger(RegistrationWindowServiceImpl.class);

    private final RegistrationWindowRepository windowRepository;
    private final SemesterRepository semesterRepository;

    public RegistrationWindowServiceImpl(RegistrationWindowRepository windowRepository,
                                         SemesterRepository semesterRepository) {
        this.windowRepository = windowRepository;
        this.semesterRepository = semesterRepository;
    }

    @Override
    public List<RegistrationWindowDTO> getAllWindows() {
        return windowRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<RegistrationWindowDTO> getCurrentlyOpenWindows() {
        return windowRepository.findAllCurrentlyOpenWindows(LocalDateTime.now()).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public RegistrationWindowDTO getWindowById(Long id) {
        RegistrationWindow window = windowRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registration window not found with ID: " + id));
        return mapToDTO(window);
    }

    @Override
    @Transactional
    public RegistrationWindowDTO createWindow(RegistrationWindowDTO dto) {
        logger.info("Admin creating registration window: '{}' for semester ID: {}", dto.getTitle(), dto.getSemesterId());

        if (dto.getStartDateTime() == null || dto.getEndDateTime() == null) {
            throw new InvalidRegistrationWindowException("Registration window start and end date times are required.");
        }
        if (!dto.getEndDateTime().isAfter(dto.getStartDateTime())) {
            throw new InvalidRegistrationWindowException("Registration window end time must be after start time.");
        }

        Semester semester = semesterRepository.findById(dto.getSemesterId())
                .orElseThrow(() -> new SemesterNotFoundException("Semester not found with ID: " + dto.getSemesterId()));

        boolean isNewWindowActive = dto.isActive() && !"CLOSED".equalsIgnoreCase(dto.getStatus());
        if (isNewWindowActive) {
            List<RegistrationWindow> existingWindows = windowRepository.findBySemesterId(semester.getId());
            for (RegistrationWindow w : existingWindows) {
                if (w.isActive() && !"CLOSED".equalsIgnoreCase(w.getStatus())) {
                    if (dto.getStartDateTime().isBefore(w.getEndDateTime()) && dto.getEndDateTime().isAfter(w.getStartDateTime())) {
                        throw new InvalidRegistrationWindowException("An active registration window already overlaps with this time period for semester " + semester.getSemesterNumber() + ".");
                    }
                }
            }
        }

        RegistrationWindow window = new RegistrationWindow(
                dto.getTitle().trim(),
                semester,
                dto.getStartDateTime(),
                dto.getEndDateTime(),
                dto.getStatus() != null ? dto.getStatus().toUpperCase() : "OPEN",
                dto.isActive()
        );

        RegistrationWindow saved = windowRepository.save(window);
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public RegistrationWindowDTO updateWindow(Long id, RegistrationWindowDTO dto) {
        RegistrationWindow window = windowRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registration window not found with ID: " + id));

        LocalDateTime finalStart = dto.getStartDateTime() != null ? dto.getStartDateTime() : window.getStartDateTime();
        LocalDateTime finalEnd = dto.getEndDateTime() != null ? dto.getEndDateTime() : window.getEndDateTime();

        if (finalStart != null && finalEnd != null && !finalEnd.isAfter(finalStart)) {
            throw new InvalidRegistrationWindowException("Registration window end time must be after start time.");
        }

        boolean willBeActive = (dto.getStatus() != null ? !"CLOSED".equalsIgnoreCase(dto.getStatus()) : !"CLOSED".equalsIgnoreCase(window.getStatus()))
                && dto.isActive();

        if (willBeActive && window.getSemester() != null) {
            List<RegistrationWindow> existingWindows = windowRepository.findBySemesterId(window.getSemester().getId());
            for (RegistrationWindow w : existingWindows) {
                if (!w.getId().equals(window.getId()) && w.isActive() && !"CLOSED".equalsIgnoreCase(w.getStatus())) {
                    if (finalStart.isBefore(w.getEndDateTime()) && finalEnd.isAfter(w.getStartDateTime())) {
                        throw new InvalidRegistrationWindowException("An active registration window already overlaps with this time period for semester " + window.getSemester().getSemesterNumber() + ".");
                    }
                }
            }
        }

        window.setTitle(dto.getTitle().trim());
        if (dto.getStartDateTime() != null) window.setStartDateTime(dto.getStartDateTime());
        if (dto.getEndDateTime() != null) window.setEndDateTime(dto.getEndDateTime());
        if (dto.getStatus() != null) window.setStatus(dto.getStatus().toUpperCase());
        window.setActive(dto.isActive());

        RegistrationWindow updated = windowRepository.save(window);
        return mapToDTO(updated);
    }

    @Override
    @Transactional
    public RegistrationWindowDTO toggleWindowStatus(Long id) {
        RegistrationWindow window = windowRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registration window not found with ID: " + id));

        if ("OPEN".equalsIgnoreCase(window.getStatus())) {
            window.setStatus("CLOSED");
        } else {
            window.setStatus("OPEN");
        }

        RegistrationWindow updated = windowRepository.save(window);
        logger.info("Admin toggled registration window {} status to {}", id, updated.getStatus());
        return mapToDTO(updated);
    }

    /**
     * CORE REQUIREMENT: Backend checks if registration window is currently open for the given semester.
     */
    @Override
    public boolean isWindowOpenForSemester(Long semesterId) {
        if (semesterId == null) {
            return false;
        }
        Optional<RegistrationWindow> openWindow = windowRepository.findOpenWindowForSemester(semesterId, LocalDateTime.now());
        return openWindow.isPresent();
    }

    @Override
    @Transactional
    public void deleteWindow(Long id) {
        if (!windowRepository.existsById(id)) {
            throw new ResourceNotFoundException("Registration window not found with ID: " + id);
        }
        windowRepository.deleteById(id);
    }

    private RegistrationWindowDTO mapToDTO(RegistrationWindow window) {
        RegistrationWindowDTO dto = new RegistrationWindowDTO();
        dto.setId(window.getId());
        dto.setTitle(window.getTitle());
        dto.setStartDateTime(window.getStartDateTime());
        dto.setEndDateTime(window.getEndDateTime());
        dto.setStatus(window.getStatus());
        dto.setActive(window.isActive());
        dto.setOpen(window.isCurrentlyOpen());

        if (window.getSemester() != null) {
            dto.setSemesterId(window.getSemester().getId());
            dto.setSemesterNumber(window.getSemester().getSemesterNumber());
        }

        return dto;
    }
}
