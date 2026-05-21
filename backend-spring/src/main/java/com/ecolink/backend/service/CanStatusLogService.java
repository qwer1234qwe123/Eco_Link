package com.ecolink.backend.service;

import com.ecolink.backend.entity.CanStatusLog;
import com.ecolink.backend.repository.CanStatusLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CanStatusLogService {

    private final CanStatusLogRepository canStatusLogRepository;

    public List<CanStatusLog> findAll() {
        return canStatusLogRepository.findAll();
    }

    public List<CanStatusLog> findByCanId(Long canId) {
        return canStatusLogRepository.findByTrashCanId(canId);
    }
}