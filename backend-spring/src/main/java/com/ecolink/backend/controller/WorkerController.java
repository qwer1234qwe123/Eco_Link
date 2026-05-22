package com.ecolink.backend.controller;

import com.ecolink.backend.dto.SignUpRequest;
import com.ecolink.backend.entity.Worker;
import com.ecolink.backend.service.WorkerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/users")
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

    @PostMapping
    public ResponseEntity<String> signUp(@RequestBody SignUpRequest request) {
        workerService.signUp(request);
        return ResponseEntity.ok("등록 완료");
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> update(@PathVariable Long id, @RequestBody SignUpRequest request) {
        workerService.update(id, request);
        return ResponseEntity.ok("수정 완료");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        workerService.delete(id);
        return ResponseEntity.ok("삭제 완료");
    }
}