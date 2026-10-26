package com.example.onlinecourse.controller;

import com.example.onlinecourse.dto.RegistrationWindowDTO;
import com.example.onlinecourse.service.RegistrationWindowService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing registration windows.
 */
@RestController
@RequestMapping("/api/registration-windows")
@CrossOrigin(origins = "*")
public class RegistrationWindowController {

    private final RegistrationWindowService registrationWindowService;

    public RegistrationWindowController(RegistrationWindowService registrationWindowService) {
        this.registrationWindowService = registrationWindowService;
    }

    @GetMapping
    public ResponseEntity<List<RegistrationWindowDTO>> getAllWindows() {
        return ResponseEntity.ok(registrationWindowService.getAllWindows());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RegistrationWindowDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(registrationWindowService.getWindowById(id));
    }

    @GetMapping("/semester/{semesterId}")
    public ResponseEntity<List<RegistrationWindowDTO>> getWindowsBySemester(@PathVariable Long semesterId) {
        // Filter by semester from all windows
        List<RegistrationWindowDTO> windows = registrationWindowService.getAllWindows().stream()
                .filter(w -> w.getSemesterId() != null && w.getSemesterId().equals(semesterId))
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(windows);
    }

    @GetMapping("/semester/{semesterId}/open")
    public ResponseEntity<Boolean> isOpen(@PathVariable Long semesterId) {
        return ResponseEntity.ok(registrationWindowService.isWindowOpenForSemester(semesterId));
    }

    @PostMapping
    public ResponseEntity<RegistrationWindowDTO> createWindow(
            @Valid @RequestBody RegistrationWindowDTO dto) {
        RegistrationWindowDTO created = registrationWindowService.createWindow(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RegistrationWindowDTO> updateWindow(
            @PathVariable Long id,
            @Valid @RequestBody RegistrationWindowDTO dto) {
        return ResponseEntity.ok(registrationWindowService.updateWindow(id, dto));
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<RegistrationWindowDTO> toggleWindow(@PathVariable Long id) {
        return ResponseEntity.ok(registrationWindowService.toggleWindowStatus(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWindow(@PathVariable Long id) {
        registrationWindowService.deleteWindow(id);
        return ResponseEntity.noContent().build();
    }
}
