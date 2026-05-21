package com.ecolink.backend.service;

import com.ecolink.backend.dto.PredictRequest;
import com.ecolink.backend.dto.PredictResponse;
import com.ecolink.backend.entity.SensorLog;
import com.ecolink.backend.entity.TrashCan;
import com.ecolink.backend.repository.SensorLogRepository;
import com.ecolink.backend.repository.TrashCanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PredictionService {

    private final RestTemplate restTemplate;
    private final SensorLogRepository sensorLogRepository;
    private final TrashCanRepository trashCanRepository;

    @Value("${ai.server.url}")
    private String aiServerUrl;

    /**
     * 특정 쓰레기통 단일 예측 요청
     */
    public PredictResponse predict(Long canId) {
        // 쓰레기통 정보 조회 (위치명 포함)
        TrashCan can = trashCanRepository.findById(canId).orElse(null);
        String locName = (can != null) ? can.getLocName() : canId + "번 쓰레기통";

        // 최근 센서 로그 5개 조회
        List<SensorLog> logs = sensorLogRepository
                .findTop5ByCanIdOrderByLogTimeDesc(canId);

        // 센서 데이터 없으면 데이터 없음 반환
        if (logs.isEmpty()) {
            return PredictResponse.builder()
                    .canId(canId)
                    .locName(locName)
                    .needsCollection(false)
                    .confidence(0.0)
                    .predictedStatus(locName)
                    .message("센서 데이터가 없습니다.")
                    .build();
        }

        SensorLog latest = logs.get(0);
        double fillRate = calculateFillRate(logs);
        double hoursSinceEmpty = calculateHoursSinceEmpty(canId);

        // FastAPI 요청 생성
        PredictRequest request = PredictRequest.builder()
                .canId(canId)
                .fillLevel(latest.getFillLevel())
                .fillRate(fillRate)
                .hoursSinceEmpty(hoursSinceEmpty)
                .hourOfDay(LocalDateTime.now().getHour())
                .batteryLevel(latest.getBatteryLevel())
                .build();

        // FastAPI 호출
        try {
            PredictResponse response = restTemplate.postForObject(
                    aiServerUrl + "/predict",
                    request,
                    PredictResponse.class);
            if (response != null) {
                response.setLocName(locName);
            }
            log.info("예측 완료 - canId: {}, 결과: {}", canId, response);
            return response;
        } catch (Exception e) {
            log.error("FastAPI 호출 실패 - canId: {}, 오류: {}", canId, e.getMessage());
            return PredictResponse.builder()
                    .canId(canId)
                    .locName(locName)
                    .needsCollection(false)
                    .confidence(0.0)
                    .predictedStatus(locName)
                    .message("AI 서버 연결에 실패했습니다.")
                    .build();
        }
    }

    /**
     * 전체 쓰레기통 일괄 예측
     */
    public List<PredictResponse> predictAll() {
        List<TrashCan> cans = trashCanRepository.findAll();
        return cans.stream()
                .map(can -> predict(can.getId()))
                .collect(Collectors.toList());
    }

    /**
     * 최근 센서 로그로 시간당 증가 속도 계산
     */
    private double calculateFillRate(List<SensorLog> logs) {
        if (logs.size() < 2)
            return 0.0;
        double first = logs.get(logs.size() - 1).getFillLevel();
        double last = logs.get(0).getFillLevel();
        int hours = logs.size() - 1;
        return Math.max(0, (last - first) / hours);
    }

    /**
     * 마지막 비움 이후 경과 시간 계산 (추후 EmptyHistory 연동)
     */
    private double calculateHoursSinceEmpty(Long canId) {
        return 999.0;
    }
}
