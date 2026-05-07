package com.ecolink.backend.repository;

import com.ecolink.backend.entity.SensorLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SensorLogRepository extends JpaRepository<SensorLog, Long> {

    // 기존에 있던 메서드들은 그대로 유지하고 아래만 추가
    List<SensorLog> findTop5ByCanIdOrderByLogTimeDesc(Long canId);

    List<SensorLog> findByTrashCanId(Long canId);

}
