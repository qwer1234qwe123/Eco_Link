package com.ecolink.backend.controller;

import com.ecolink.backend.dto.SignUpRequest;
import com.ecolink.backend.entity.Worker;
import com.ecolink.backend.service.WorkerService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/worker")
@RequiredArgsConstructor

public class WorkerController{
    private final WorkerService WorkerService;

    @GetMapping
    public ResponseEntity<List<Worker>> findAll(){
        return ResponseEntity.ok(WorkerService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Worker> findById(@PathVariable Long id){
        return ResponseEntity.ok(WorkerService.findById(id));
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signUp(@RequestBody SignUpRequest request){
        WorkerService.signUp(request);
        return ResponseEntity.ok("회원가입 완료");
    }
}