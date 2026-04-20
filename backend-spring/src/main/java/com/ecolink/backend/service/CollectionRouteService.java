package com.ecolink.backend.service;

import com.ecolink.backend.entity.CollectionRoute;
import com.ecolink.backend.repository.CollectionRouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CollectionRouteService {

    private final CollectionRouteRepository collectionRouteRepository;

    public List<CollectionRoute> findAll() {
        return collectionRouteRepository.findAll();
    }

    public List<CollectionRoute> findByWorkerId(Long workerId) {
        return collectionRouteRepository.findByWorkerId(workerId);
    }

    public CollectionRoute findById(Long id) {
        return collectionRouteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("수거 경로를 찾을 수 없습니다. id: " + id));
    }
}