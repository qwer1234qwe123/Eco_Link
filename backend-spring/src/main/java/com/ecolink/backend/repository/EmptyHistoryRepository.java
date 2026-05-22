package com.ecolink.backend.repository;

import com.ecolink.backend.entity.EmptyHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EmptyHistoryRepository extends JpaRepository<EmptyHistory, Long> {

    List<EmptyHistory> findByTrashCanId(Long canId);

    List<EmptyHistory> findTop1ByTrashCanIdOrderByEmptiedAtDesc(Long canId);

    List<EmptyHistory> findByTrashCanIdOrderByEmptiedAtDesc(Long canId);
}
