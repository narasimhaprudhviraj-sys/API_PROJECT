package com.example.onlinecourse.controller;

import com.example.onlinecourse.dto.MinorStreamDTO;
import com.example.onlinecourse.service.MinorStreamService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing minor streams.
 */
@RestController
@RequestMapping("/api/minor-streams")
@CrossOrigin(origins = "*")
public class MinorStreamController {

    private final MinorStreamService minorStreamService;

    public MinorStreamController(MinorStreamService minorStreamService) {
        this.minorStreamService = minorStreamService;
    }

    @GetMapping
    public ResponseEntity<List<MinorStreamDTO>> getAllMinorStreams() {
        return ResponseEntity.ok(minorStreamService.getAllMinorStreams());
    }

    @GetMapping("/active")
    public ResponseEntity<List<MinorStreamDTO>> getActiveMinorStreams() {
        List<MinorStreamDTO> active = minorStreamService.getAllMinorStreams().stream()
                .filter(MinorStreamDTO::isActive)
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(active);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MinorStreamDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(minorStreamService.getMinorStreamById(id));
    }

    @GetMapping("/branch/{branchId}")
    public ResponseEntity<List<MinorStreamDTO>> getByBranch(@PathVariable Long branchId) {
        return ResponseEntity.ok(minorStreamService.getMinorStreamsByBranch(branchId));
    }

    @PostMapping
    public ResponseEntity<MinorStreamDTO> createMinorStream(@Valid @RequestBody MinorStreamDTO minorStreamDTO) {
        MinorStreamDTO created = minorStreamService.createMinorStream(minorStreamDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MinorStreamDTO> updateMinorStream(@PathVariable Long id,
                                                             @Valid @RequestBody MinorStreamDTO minorStreamDTO) {
        return ResponseEntity.ok(minorStreamService.updateMinorStream(id, minorStreamDTO));
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<Void> toggleStatus(@PathVariable Long id) {
        minorStreamService.toggleMinorStreamStatus(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMinorStream(@PathVariable Long id) {
        minorStreamService.deleteMinorStream(id);
        return ResponseEntity.noContent().build();
    }
}
