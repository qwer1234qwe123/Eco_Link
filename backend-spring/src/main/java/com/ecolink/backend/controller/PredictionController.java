package com.ecolink.backend.controller;

import com.ecolink.backend.dto.PredictResponse;
import com.ecolink.backend.service.PredictionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prediction")
@RequiredArgsConstructor
public class PredictionController {

    private final PredictionService predictionService;

    /**
     * 단일 쓰레기통 예측
     * GET /api/prediction/{canId}
     */
    @GetMapping("/{canId}")
    public ResponseEntity<PredictResponse> predict(@PathVariable Long canId) {
        PredictResponse response = predictionService.predict(canId);
        return ResponseEntity.ok(response);
    }

    /**
     * 전체 쓰레기통 일괄 예측
     * GET /api/prediction/all
     */
    @GetMapping("/all")
    public ResponseEntity<List<PredictResponse>> predictAll() {
        List<PredictResponse> responses = predictionService.predictAll();
        return ResponseEntity.ok(responses);
    }
}
