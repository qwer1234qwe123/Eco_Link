package com.ecolink.backend.controller;

import com.ecolink.backend.entity.CollectionHistory;
import com.ecolink.backend.service.CollectionHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class CollectionHistoryController {

    private final CollectionHistoryService collectionHistoryService;

    @GetMapping
    public ResponseEntity<List<CollectionHistory>> findAll() {
        return ResponseEntity.ok(collectionHistoryService.findAll());
    }

    @GetMapping("/route/{routeId}")
    public ResponseEntity<List<CollectionHistory>> findByRouteId(@PathVariable Long routeId) {
        return ResponseEntity.ok(collectionHistoryService.findByRouteId(routeId));
    }

    @GetMapping("/can/{canId}")
    public ResponseEntity<List<CollectionHistory>> findByCanId(@PathVariable Long canId) {
        return ResponseEntity.ok(collectionHistoryService.findByCanId(canId));
    }
}
