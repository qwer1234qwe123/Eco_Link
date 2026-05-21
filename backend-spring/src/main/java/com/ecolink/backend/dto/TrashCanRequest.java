package com.ecolink.backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor          // 기본 생성자 자동 생성
public class TrashCanRequest {
    private String locName;     // 위치 이름
    private Double locLat;      // 위도
    private Double locLng;      // 경도
    private Integer maxCapa;    // 최대 용량
}