package com.example.onlinecourse.service;

import com.example.onlinecourse.dto.RegistrationDTO;
import com.example.onlinecourse.dto.RegistrationResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RegistrationService {

    /**
     * CORE REQUIREMENT: Transactional Course Registration with 6 validations:
     * 1. Window open check
     * 2. Branch & Fixed Minor Stream eligibility
     * 3. Semester matching
     * 4. Duplicate registration check
     * 5. Seat availability check
     * 6. Atomic registration creation + seat decrement
     */
    RegistrationResponseDTO registerCourse(RegistrationDTO registrationDTO);

    RegistrationResponseDTO getRegistrationById(Long id);

    List<RegistrationResponseDTO> getAllRegistrations();

    Page<RegistrationResponseDTO> getRegistrationsPage(Pageable pageable);

    List<RegistrationResponseDTO> getStudentRegistrations(Long studentId);

    List<RegistrationResponseDTO> getCourseRegistrations(Long courseId);

    /**
     * CORE REQUIREMENT: Transactional Course Drop with ownership validation
     * and atomic seat increment.
     */
    void cancelRegistration(Long id);

    void dropCourse(Long registrationId, Long studentId);
}
