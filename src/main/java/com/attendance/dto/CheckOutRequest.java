package com.attendance.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckOutRequest {

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    private String remarks;
}
