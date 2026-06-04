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
<<<<<<< HEAD

    private String fingerprintData;

    // Set by controller when authentication token is present and valid
    @Builder.Default
    private boolean webAuthnAuthenticated = false;
=======
>>>>>>> a51b55306b65905891f055e408b3cf0c90e98396
}
