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
    
    // New fields for dynamic dashboard
    private long weeklyOffs;
    private long holidays;
    private long checkedOutToday;
    private long earlyGoingToday;
    private long pendingLeaveRequests;
    private long regularizationRequests;
}
