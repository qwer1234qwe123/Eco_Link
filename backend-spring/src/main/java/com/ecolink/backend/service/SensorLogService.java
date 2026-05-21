package com.ecolink.backend.service;

import com.ecolink.backend.entity.SensorLog;
import com.ecolink.backend.entity.TrashCan;
import com.ecolink.backend.repository.SensorLogRepository;
import com.ecolink.backend.repository.TrashCanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SensorLogService {

    private final SensorLogRepository sensorLogRepository;
    private final TrashCanRepository trashCanRepository;

    public List<SensorLog> findAll() {
        return sensorLogRepository.findAll();
    }

    public List<SensorLog> findByCanId(Long canId) {
        return sensorLogRepository.findByTrashCanId(canId);
    }

    public void save(Long canId, Integer fillLevel, Integer batteryLevel) {
        TrashCan trashCan = trashCanRepository.findById(canId)
                .orElseThrow(() -> new RuntimeException("쓰레기통을 찾을 수 없습니다. id: " + canId));
        SensorLog log = new SensorLog(trashCan, fillLevel, batteryLevel);
        sensorLogRepository.save(log);
    }
}
