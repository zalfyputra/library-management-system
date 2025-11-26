package com.library.dto.response;

import com.library.domain.enums.AuditAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogResponse {
    private Long id;
    private Long userId;
    private String username;
    private AuditAction action;
    private String description;
    private String resourceType;
    private Long resourceId;
    private String ipAddress;
    private String browser;
    private String device;
    private String operatingSystem;
    private Boolean success;
    private String details;
    private LocalDateTime timestamp;
}
