package com.example.onlinecourse.service;

import com.example.onlinecourse.dto.RegistrationWindowDTO;
import com.example.onlinecourse.entity.RegistrationWindow;
import com.example.onlinecourse.entity.Semester;
import com.example.onlinecourse.exception.InvalidRegistrationWindowException;
import com.example.onlinecourse.repository.RegistrationWindowRepository;
import com.example.onlinecourse.repository.SemesterRepository;
import com.example.onlinecourse.service.impl.RegistrationWindowServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RegistrationWindowServiceTest {

    @Mock
    private RegistrationWindowRepository windowRepository;

    @Mock
    private SemesterRepository semesterRepository;

    @InjectMocks
    private RegistrationWindowServiceImpl windowService;

    private Semester semester;
    private RegistrationWindow window;
    private RegistrationWindowDTO windowDTO;

    @BeforeEach
    void setUp() {
        semester = new Semester(3, "2025-26", true);
        semester.setId(1L);

        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(10);

        window = new RegistrationWindow("Sem 3 Window", semester, start, end, "OPEN", true);
        window.setId(1L);

        windowDTO = new RegistrationWindowDTO();
        windowDTO.setTitle("Sem 3 Window");
        windowDTO.setSemesterId(1L);
        windowDTO.setStartDateTime(start);
        windowDTO.setEndDateTime(end);
        windowDTO.setStatus("OPEN");
        windowDTO.setActive(true);
    }

    @Test
    @DisplayName("Should create window successfully when dates and semester are valid")
    void testCreateWindow_Success() {
        when(semesterRepository.findById(1L)).thenReturn(Optional.of(semester));
        when(windowRepository.findBySemesterId(1L)).thenReturn(List.of());
        when(windowRepository.save(any(RegistrationWindow.class))).thenReturn(window);

        RegistrationWindowDTO result = windowService.createWindow(windowDTO);

        assertNotNull(result);
        assertEquals("Sem 3 Window", result.getTitle());
        verify(windowRepository, times(1)).save(any(RegistrationWindow.class));
    }

    @Test
    @DisplayName("Should throw InvalidRegistrationWindowException when end date is before start date")
    void testCreateWindow_InvalidDates() {
        windowDTO.setEndDateTime(windowDTO.getStartDateTime().minusDays(1));

        assertThrows(InvalidRegistrationWindowException.class, () -> windowService.createWindow(windowDTO));
        verify(windowRepository, never()).save(any(RegistrationWindow.class));
    }

    @Test
    @DisplayName("Should throw InvalidRegistrationWindowException when overlapping window exists for semester")
    void testCreateWindow_OverlappingWindow() {
        when(semesterRepository.findById(1L)).thenReturn(Optional.of(semester));
        when(windowRepository.findBySemesterId(1L)).thenReturn(List.of(window));

        assertThrows(InvalidRegistrationWindowException.class, () -> windowService.createWindow(windowDTO));
        verify(windowRepository, never()).save(any(RegistrationWindow.class));
    }

    @Test
    @DisplayName("Should return true when registration window is actively open for semester")
    void testIsWindowOpenForSemester_Open() {
        when(windowRepository.findOpenWindowForSemester(eq(1L), any(LocalDateTime.class)))
                .thenReturn(Optional.of(window));

        boolean isOpen = windowService.isWindowOpenForSemester(1L);

        assertTrue(isOpen);
    }

    @Test
    @DisplayName("Should return false when registration window is not open for semester")
    void testIsWindowOpenForSemester_Closed() {
        when(windowRepository.findOpenWindowForSemester(eq(1L), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        boolean isOpen = windowService.isWindowOpenForSemester(1L);

        assertFalse(isOpen);
    }
}
