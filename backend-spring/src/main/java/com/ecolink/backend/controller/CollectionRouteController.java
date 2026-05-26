package com.ecolink.backend.controller;

import com.ecolink.backend.entity.CollectionRoute;
import com.ecolink.backend.service.CollectionRouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.util.List;

@RestController
@RequestMapping("/api/route")
@RequiredArgsConstructor
public class CollectionRouteController {

    private final CollectionRouteService collectionRouteService;
    private final RestTemplate restTemplate;

    @Value("${kakao.rest.api.key}")
    private String kakaoRestApiKey;

    @GetMapping
    public ResponseEntity<List<CollectionRoute>> findAll() {
        return ResponseEntity.ok(collectionRouteService.findAll());
    }

    @GetMapping("/worker/{workerId}")
    public ResponseEntity<List<CollectionRoute>> findByWorkerId(@PathVariable Long workerId) {
        return ResponseEntity.ok(collectionRouteService.findByWorkerId(workerId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CollectionRoute> findById(@PathVariable Long id) {
        return ResponseEntity.ok(collectionRouteService.findById(id));
    }

    // ── 카카오 모빌리티 경로 탐색 ──
    @GetMapping("/directions")
    public ResponseEntity<?> getDirections(
            @RequestParam String origin,
            @RequestParam String destination,
            @RequestParam(required = false) String waypoints) {
        try {
            StringBuilder url = new StringBuilder(
                    "https://apis-navi.kakaomobility.com/v1/directions"
                            + "?origin=" + origin
                            + "&destination=" + destination);
            if (waypoints != null && !waypoints.isEmpty()) {
                url.append("&waypoints=").append(waypoints);
            }
            url.append("&summary=false");

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "KakaoAK " + kakaoRestApiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            ResponseEntity<String> response = restTemplate.exchange(
                    url.toString(),
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    String.class);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response.getBody());

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("{\"error\": \"경로 조회 실패: " + e.getMessage() + "\"}");
        }
    }
}