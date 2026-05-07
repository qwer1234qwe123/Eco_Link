package com.ecolink.backend.controller;

import com.ecolink.backend.entity.TrashCan;
import com.ecolink.backend.service.TrashCanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/trashcan")
@RequiredArgsConstructor
public class TrashCanController {

    private final TrashCanService trashCanService;

    @GetMapping
    public ResponseEntity<List<TrashCan>> findAll() {
        return ResponseEntity.ok(trashCanService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrashCan> findById(@PathVariable Long id) {
        return ResponseEntity.ok(trashCanService.findById(id));
    }
}