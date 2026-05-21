package com.ecolink.backend.controller;

import com.ecolink.backend.entity.TrashCan;
import com.ecolink.backend.service.TrashCanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

// 추가
import com.ecolink.backend.dto.TrashCanRequest;

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


    // 추가
    @PostMapping
public ResponseEntity<TrashCan> create(@RequestBody TrashCanRequest request) {
    return ResponseEntity.ok(trashCanService.create(request));
}

@PutMapping("/{id}")
public ResponseEntity<TrashCan> update(@PathVariable Long id, @RequestBody TrashCanRequest request) {
    return ResponseEntity.ok(trashCanService.update(id, request));
}

@DeleteMapping("/{id}")
public ResponseEntity<Void> delete(@PathVariable Long id) {
    trashCanService.delete(id);
    return ResponseEntity.noContent().build();
}
}