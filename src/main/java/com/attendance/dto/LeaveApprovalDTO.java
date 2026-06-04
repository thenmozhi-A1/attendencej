package com.attendance.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveApprovalDTO {

    @NotNull(message = "Approval status is required")
    private String status;

    private String approvalRemarks;
}
