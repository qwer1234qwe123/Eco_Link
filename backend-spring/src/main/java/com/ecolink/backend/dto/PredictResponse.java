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
public class PredictResponse {

    @JsonProperty("can_id")
    private Long canId;

    @JsonProperty("loc_name")
    private String locName;

    @JsonProperty("needs_collection")
    private boolean needsCollection;

    @JsonProperty("confidence")
    private double confidence;

    @JsonProperty("predicted_status")
    private String predictedStatus;

    @JsonProperty("message")
    private String message;
}
