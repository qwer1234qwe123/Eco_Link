package com.ecolink.backend.controller;

import com.ecolink.backend.entity.CollectionRoute;
import com.ecolink.backend.service.CollectionRouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/route")
@RequiredArgsConstructor
public class CollectionRouteController {

    private final CollectionRouteService collectionRouteService;

    @GetMapping
    public ResponseEntity<List<CollectionRoute>> findAll() {
        return ResponseEntity.ok(collectionRouteService.findAll());
    }

    @GetMapping("/worker/{workerId}")
    public ResponseEntity<List<CollectionRoute>> findByWorkerId(@PathVariable Long workerId) {
        return ResponseEntity.ok(collectionRouteService.findByWorkerId(workerId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CollectionRoute> findById(@PathVariable Long id) {
        return ResponseEntity.ok(collectionRouteService.findById(id));
    }
}