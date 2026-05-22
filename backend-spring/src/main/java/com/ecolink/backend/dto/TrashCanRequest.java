package com.ecolink.backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TrashCanRequest {
    private String locName;
    private Double locLat;
    private Double locLng;
    private Integer maxCapa;
}
