package com.library.controller;

import com.library.domain.enums.AuditAction;
import com.library.dto.response.ApiResponse;
import com.library.dto.response.AuditLogResponse;
import com.library.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/audit-logs")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Audit Logs", description = "Audit log operations (SUPER_ADMIN only)")
public class AuditLogController {
    
    @Autowired
    private AuditLogService auditLogService;
    
    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get all audit logs", description = "Retrieve all audit logs with pagination (SUPER_ADMIN only)")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getAllLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLogResponse> logs = auditLogService.getAllLogs(pageable);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }
    
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get logs by user", description = "Retrieve audit logs for a specific user (SUPER_ADMIN only)")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getLogsByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLogResponse> logs = auditLogService.getLogsByUserId(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }
    
    @GetMapping("/action/{action}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get logs by action", description = "Retrieve audit logs by action type (SUPER_ADMIN only)")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getLogsByAction(
            @PathVariable AuditAction action,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLogResponse> logs = auditLogService.getLogsByAction(action, pageable);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }
    
    @GetMapping("/date-range")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get logs by date range", description = "Retrieve audit logs within a date range (SUPER_ADMIN only)")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLogResponse> logs = auditLogService.getLogsByDateRange(startDate, endDate, pageable);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }
}

