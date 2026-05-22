package com.ecolink.backend.service;

import com.ecolink.backend.dto.PredictRequest;
import com.ecolink.backend.dto.PredictResponse;
import com.ecolink.backend.entity.EmptyHistory;
import com.ecolink.backend.entity.SensorLog;
import com.ecolink.backend.entity.TrashCan;
import com.ecolink.backend.repository.EmptyHistoryRepository;
import com.ecolink.backend.repository.SensorLogRepository;
import com.ecolink.backend.repository.TrashCanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PredictionService {

    private final RestTemplate restTemplate;
    private final SensorLogRepository sensorLogRepository;
    private final TrashCanRepository trashCanRepository;
    private final EmptyHistoryRepository emptyHistoryRepository;

    @Value("${ai.server.url}")
    private String aiServerUrl;

    public PredictResponse predict(Long canId) {
        TrashCan can = trashCanRepository.findById(canId).orElse(null);
        String locName = (can != null) ? can.getLocName() : canId + "번 쓰레기통";

        List<SensorLog> logs = sensorLogRepository
                .findTop5ByCanIdOrderByLogTimeDesc(canId);

        if (logs.isEmpty()) {
            return PredictResponse.builder()
                    .canId(canId)
                    .locName(locName)
                    .needsCollection(false)
                    .confidence(0.0)
                    .predictedStatus("정상") // ← 수정
                    .message("센서 데이터가 없습니다.")
                    .build();
        }

        SensorLog latest = logs.get(0);
        double fillRate = calculateFillRate(logs);
        double hoursSinceEmpty = calculateHoursSinceEmpty(canId);

        PredictRequest request = PredictRequest.builder()
                .canId(canId)
                .fillLevel(latest.getFillLevel())
                .fillRate(fillRate)
                .hoursSinceEmpty(hoursSinceEmpty)
                .hourOfDay(LocalDateTime.now().getHour())
                .batteryLevel(latest.getBatteryLevel())
                .build();

        try {
            PredictResponse response = restTemplate.postForObject(
                    aiServerUrl + "/predict",
                    request,
                    PredictResponse.class);
            if (response != null) {
                response.setLocName(locName);
            }
            log.info("예측 완료 - canId: {}, hoursSinceEmpty: {}h, 결과: {}",
                    canId, hoursSinceEmpty, response);
            return response;
        } catch (Exception e) {
            log.error("FastAPI 호출 실패 - canId: {}, 오류: {}", canId, e.getMessage());
            return PredictResponse.builder()
                    .canId(canId)
                    .locName(locName)
                    .needsCollection(false)
                    .confidence(0.0)
                    .predictedStatus("정상") // ← 수정
                    .message("AI 서버 연결에 실패했습니다.")
                    .build();
        }
    }

    public List<PredictResponse> predictAll() {
        List<TrashCan> cans = trashCanRepository.findAll();
        return cans.stream()
                .map(can -> predict(can.getId()))
                .collect(Collectors.toList());
    }

    private double calculateFillRate(List<SensorLog> logs) {
        if (logs.size() < 2)
            return 0.0;
        double first = logs.get(logs.size() - 1).getFillLevel();
        double last = logs.get(0).getFillLevel();
        int hours = logs.size() - 1;
        return Math.max(0, (last - first) / hours);
    }

    private double calculateHoursSinceEmpty(Long canId) {
        List<EmptyHistory> empties = emptyHistoryRepository
                .findTop1ByTrashCanIdOrderByEmptiedAtDesc(canId);

        if (empties.isEmpty()) {
            log.debug("비움 기록 없음 - canId: {}, hours_since_empty: 999.0", canId);
            return 999.0;
        }

        LocalDateTime lastEmpty = empties.get(0).getEmptiedAt();
        double hours = ChronoUnit.MINUTES.between(lastEmpty, LocalDateTime.now()) / 60.0;
        log.debug("마지막 비움 시각: {}, 경과 시간: {}h", lastEmpty, hours);
        return Math.round(hours * 100.0) / 100.0;
    }
}