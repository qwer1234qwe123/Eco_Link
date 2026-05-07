package com.ecolink.backend.repository;

import com.ecolink.backend.entity.SensorLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SensorLogRepository extends JpaRepository<SensorLog, Long> {
    List<SensorLog> findByTrashCanId(Long canId);
}
