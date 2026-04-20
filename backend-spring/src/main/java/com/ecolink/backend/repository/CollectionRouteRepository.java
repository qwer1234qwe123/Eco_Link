package com.ecolink.backend.repository;

import com.ecolink.backend.entity.CollectionRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CollectionRouteRepository extends JpaRepository<CollectionRoute, Long> {
    List<CollectionRoute> findByWorkerId(Long workerId);
}