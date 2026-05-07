package com.ecolink.backend.controller;

import com.ecolink.backend.entity.CanStatusLog;
import com.ecolink.backend.service.CanStatusLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/status")
@RequiredArgsConstructor
public class CanStatusLogController {

    private final CanStatusLogService canStatusLogService;

    @GetMapping
    public ResponseEntity<List<CanStatusLog>> findAll() {
        return ResponseEntity.ok(canStatusLogService.findAll());
    }

    @GetMapping("/{canId}")
    public ResponseEntity<List<CanStatusLog>> findByCanId(@PathVariable Long canId) {
        return ResponseEntity.ok(canStatusLogService.findByCanId(canId));
    }
}