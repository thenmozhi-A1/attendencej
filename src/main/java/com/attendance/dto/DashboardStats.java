package com.attendance.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStats {

    private long totalEmployees;
    private long presentToday;
    private long absentToday;
    private long lateToday;
    private long onLeaveToday;
}
