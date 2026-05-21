package com.ecolink.backend.controller;

import com.ecolink.backend.entity.Worker;
import com.ecolink.backend.service.WorkerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/worker")
@RequiredArgsConstructor
public class WorkerController {

    private final WorkerService workerService;

    @GetMapping
    public ResponseEntity<List<Worker>> findAll() {
        return ResponseEntity.ok(workerService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Worker> findById(@PathVariable Long id) {
        return ResponseEntity.ok(workerService.findById(id));
    }
}