package com.ecolink.backend.controller;

import com.ecolink.backend.dto.SensorLogRequest;
import com.ecolink.backend.entity.SensorLog;
import com.ecolink.backend.service.SensorLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/sensor")
@RequiredArgsConstructor
public class SensorLogController {

    private final SensorLogService sensorLogService;

    @GetMapping
    public ResponseEntity<List<SensorLog>> findAll() {
        return ResponseEntity.ok(sensorLogService.findAll());
    }

    @GetMapping("/{canId}")
    public ResponseEntity<List<SensorLog>> findByCanId(@PathVariable Long canId) {
        return ResponseEntity.ok(sensorLogService.findByCanId(canId));
    }

    @PostMapping("/log")
    public ResponseEntity<String> save(@RequestBody SensorLogRequest request) {
        sensorLogService.save(request.getCanId(), request.getFillLevel(), request.getBatteryLevel());
        return ResponseEntity.ok("저장 완료");
    }
}