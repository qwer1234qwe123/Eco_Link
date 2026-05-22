package com.ecolink.backend.controller;

import com.ecolink.backend.entity.EmptyHistory;
import com.ecolink.backend.repository.EmptyHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/empty-history")
@RequiredArgsConstructor
public class EmptyHistoryController {

    private final EmptyHistoryRepository emptyHistoryRepository;

    @GetMapping
    public ResponseEntity<List<EmptyHistory>> findAll() {
        return ResponseEntity.ok(emptyHistoryRepository.findAll());
    }

    @GetMapping("/can/{canId}")
    public ResponseEntity<List<EmptyHistory>> findByCanId(@PathVariable Long canId) {
        return ResponseEntity.ok(
                emptyHistoryRepository.findByTrashCanIdOrderByEmptiedAtDesc(canId));
    }
}