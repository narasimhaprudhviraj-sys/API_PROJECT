package com.example.onlinecourse.service;

import com.example.onlinecourse.dto.RegistrationWindowDTO;
import java.util.List;

public interface RegistrationWindowService {
    List<RegistrationWindowDTO> getAllWindows();
    List<RegistrationWindowDTO> getCurrentlyOpenWindows();
    RegistrationWindowDTO getWindowById(Long id);
    RegistrationWindowDTO createWindow(RegistrationWindowDTO dto);
    RegistrationWindowDTO updateWindow(Long id, RegistrationWindowDTO dto);
    RegistrationWindowDTO toggleWindowStatus(Long id);
    boolean isWindowOpenForSemester(Long semesterId);
    void deleteWindow(Long id);
}
