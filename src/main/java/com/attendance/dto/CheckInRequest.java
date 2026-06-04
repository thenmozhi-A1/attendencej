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
    private String fingerprintData;

    // Set by controller when authentication token is present and valid
    @Builder.Default
    private boolean webAuthnAuthenticated = false;
}
