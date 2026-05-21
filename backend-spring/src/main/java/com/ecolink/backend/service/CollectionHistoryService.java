package com.ecolink.backend.service;

import com.ecolink.backend.entity.CollectionHistory;
import com.ecolink.backend.repository.CollectionHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CollectionHistoryService {

    private final CollectionHistoryRepository collectionHistoryRepository;

    public List<CollectionHistory> findAll() {
        return collectionHistoryRepository.findAll();
    }

    public List<CollectionHistory> findByRouteId(Long routeId) {
        return collectionHistoryRepository.findByCollectionRouteId(routeId);
    }

    public List<CollectionHistory> findByCanId(Long canId) {
        return collectionHistoryRepository.findByTrashCanId(canId);
    }
}