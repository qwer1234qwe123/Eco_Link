package com.ecolink.backend.controller;

import com.ecolink.backend.dto.TrashCanRequest;
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

    // 추가
    // POST /api/trashcan > 새 쓰레기통 추가
    @PostMapping
    public ResponseEntity<TrashCan> create(@RequestBody TrashCanRequest request) {
        return ResponseEntity.ok(trashCanService.create(request));
    }

    // PUT /api/trashcan/{id} > id에 해당하는 쓰레기통 수정
    @PutMapping("/{id}")
    public ResponseEntity<TrashCan> update(@PathVariable Long id, @RequestBody TrashCanRequest request) {
        return ResponseEntity.ok(trashCanService.update(id, request));
    }

    // DELECT /api/trashcan/{id} > id 에 해당하는 쓰레기통 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        trashCanService.delete(id);
        return ResponseEntity.noContent().build();
    }
}