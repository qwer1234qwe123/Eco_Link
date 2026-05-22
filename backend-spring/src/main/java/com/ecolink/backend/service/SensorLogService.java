package com.ecolink.backend.service;

import com.ecolink.backend.entity.EmptyHistory;
import com.ecolink.backend.entity.SensorLog;
import com.ecolink.backend.entity.TrashCan;
import com.ecolink.backend.repository.EmptyHistoryRepository;
import com.ecolink.backend.repository.SensorLogRepository;
import com.ecolink.backend.repository.TrashCanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SensorLogService {

    private final SensorLogRepository sensorLogRepository;
    private final TrashCanRepository trashCanRepository;
    private final EmptyHistoryRepository emptyHistoryRepository;

    public List<SensorLog> findAll() {
        return sensorLogRepository.findAll();
    }

    public List<SensorLog> findByCanId(Long canId) {
        return sensorLogRepository.findByTrashCanId(canId);
    }

    public void save(Long canId, Integer fillLevel, Integer batteryLevel) {
        TrashCan trashCan = trashCanRepository.findById(canId)
                .orElseThrow(() -> new RuntimeException("쓰레기통을 찾을 수 없습니다. id: " + canId));

        // ── 수거 감지 로직 ──
        List<SensorLog> recentLogs = sensorLogRepository
                .findTop1ByTrashCanIdOrderByLogTimeDesc(canId);

        if (!recentLogs.isEmpty()) {
            int prevLevel = recentLogs.get(0).getFillLevel();

            // 현재 5% 이하 AND 이전 20% 초과 AND 15% 이상 감소 → 수거 발생으로 판단
            if (fillLevel <= 5
                    && prevLevel > 20
                    && (prevLevel - fillLevel) >= 15) {

                EmptyHistory history = EmptyHistory.builder()
                        .trashCan(trashCan)
                        .beforeLevel((float) prevLevel)
                        .afterLevel((float) fillLevel)
                        .note("센서 자동 감지")
                        .build();
                emptyHistoryRepository.save(history);
                log.info("수거 감지 - canId: {}, 이전: {}% → 현재: {}%",
                        canId, prevLevel, fillLevel);
            }
        }

        // 센서 데이터 저장
        SensorLog sensorLog = new SensorLog(trashCan, fillLevel, batteryLevel);
        sensorLogRepository.save(sensorLog);
    }
}