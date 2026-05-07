package com.ecolink.backend.repository;

import com.ecolink.backend.entity.CollectionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CollectionHistoryRepository extends JpaRepository<CollectionHistory, Long> {
    List<CollectionHistory> findByCollectionRouteId(Long routeId);
    List<CollectionHistory> findByTrashCanId(Long canId);
}