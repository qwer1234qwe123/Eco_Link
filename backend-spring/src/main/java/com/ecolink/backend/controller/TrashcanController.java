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
    // [ 생성 ] POST /api/trashcan > 새 쓰레기통 추가
    // 프론트에서 보낸 쓰레기통 정보 (이름, 위도, 경도, 용량)등을 받아서 DB에 저장
    @PostMapping
public ResponseEntity<TrashCan> create(@RequestBody TrashCanRequest request) {
    return ResponseEntity.ok(trashCanService.create(request));
}

// [ 수정 ] PUT /api/trashcan/{id} > id에 해당하는 쓰레기통 수정
// URL의 id로 쓰레기통을 찾아서 프론트에서 보낸 새 정보로 DB 업데이트
@PutMapping("/{id}")
public ResponseEntity<TrashCan> update(@PathVariable Long id, @RequestBody TrashCanRequest request) {
    return ResponseEntity.ok(trashCanService.update(id, request));
}

// [ 삭제 ] DELECT /api/trashcan/{id} > id 에 해당하는 쓰레기통 삭제
// URL의 id로 쓰레기통을 찾아서 DB에서 삭제, 완료되면 빈 응답(204) 반환한다.
@DeleteMapping("/{id}")
public ResponseEntity<Void> delete(@PathVariable Long id) {
    trashCanService.delete(id);
    return ResponseEntity.noContent().build();
}
}