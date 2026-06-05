package com.attendance.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckInRequest {

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    private String remarks;
    private String webAuthnToken;

    // Set by service after validating the short-lived WebAuthn token
    @Builder.Default
    private boolean webAuthnAuthenticated = false;
}
