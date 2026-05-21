package com.ecolink.backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SensorLogRequest {
    private Long canId;
    private Integer fillLevel;
    private Integer batteryLevel;
}