package com.attendance.dto;

import lombok.*;
import com.fasterxml.jackson.annotation.JsonInclude;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TechLoginRequest {
    private String username;
    private String credential;
}
