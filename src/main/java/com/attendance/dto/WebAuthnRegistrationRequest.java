package com.attendance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebAuthnRegistrationRequest {

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    @NotBlank(message = "Credential ID is required")
    private String credentialId;

    @NotBlank(message = "Public key is required")
    private String publicKey;

    private long signCount;

    private String transports;

    private String deviceName;
}
