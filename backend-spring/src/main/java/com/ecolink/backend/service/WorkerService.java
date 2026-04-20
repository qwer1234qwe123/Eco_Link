package com.ecolink.backend.service;

import com.ecolink.backend.entity.Worker;
import com.ecolink.backend.repository.WorkerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkerService {

    private final WorkerRepository workerRepository;

    public List<Worker> findAll() {
        return workerRepository.findAll();
    }

    public Worker findById(Long id) {
        return workerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("작업자를 찾을 수 없습니다. id: " + id));
    }
}