package com.ecolink.backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor

public class SignUpRequest {
    private String username;
    private String password;
    private String vehicleNumber;
}
