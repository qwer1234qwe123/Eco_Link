package com.ecolink.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictRequest {

    @JsonProperty("can_id")
    private Long canId;

    @JsonProperty("fill_level")
    private double fillLevel;

    @JsonProperty("fill_rate")
    private double fillRate;

    @JsonProperty("hours_since_empty")
    private double hoursSinceEmpty;

    @JsonProperty("hour_of_day")
    private int hourOfDay;

    @JsonProperty("battery_level")
    private double batteryLevel;
}
