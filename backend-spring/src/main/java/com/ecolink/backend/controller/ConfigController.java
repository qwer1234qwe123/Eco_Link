package com.ecolink.backend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
public class ConfigController {

    @Value("${kakao.map.key}")
    private String kakaoMapKey;

    @GetMapping("/api/config/kakao-key")
    public ResponseEntity<Map<String, String>> getKakaoKey() {
        Map<String, String> config = new HashMap<>();
        config.put("key", kakaoMapKey);
        return ResponseEntity.ok(config);
    }
}
