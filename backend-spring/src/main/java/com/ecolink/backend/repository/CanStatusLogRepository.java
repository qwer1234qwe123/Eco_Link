package com.ecolink.backend.repository;

import com.ecolink.backend.entity.CanStatusLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CanStatusLogRepository extends JpaRepository<CanStatusLog, Long> {
    List<CanStatusLog> findByTrashCanId(Long canId);
}