package com.library.service;

import com.library.domain.entity.AuditLog;
import com.library.domain.enums.AuditAction;
import com.library.dto.response.AuditLogResponse;
import com.library.repository.AuditLogRepository;
import com.library.util.DeviceUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class AuditLogService {
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    @Autowired
    private DeviceUtil deviceUtil;
    
    public void log(AuditAction action, String description, Long userId, String username, 
                    String resourceType, Long resourceId, Boolean success, String details) {
        HttpServletRequest request = getCurrentRequest();
        
        if (request == null) {
            logWithoutRequest(action, description, userId, username, resourceType, resourceId, success, details);
            return;
        }
        
        String ipAddress = deviceUtil.getClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        Map<String, String> deviceInfo = deviceUtil.extractDeviceInfo(request);
        
        AuditLog auditLog = AuditLog.builder()
                .userId(userId)
                .username(username)
                .action(action)
                .description(description)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .browser(deviceInfo.get("browser"))
                .device(deviceInfo.get("device"))
                .operatingSystem(deviceInfo.get("os"))
                .success(success)
                .details(details)
                .build();
        
        auditLogRepository.save(auditLog);
    }
    
    private void logWithoutRequest(AuditAction action, String description, Long userId, String username,
                                   String resourceType, Long resourceId, Boolean success, String details) {
        AuditLog auditLog = AuditLog.builder()
                .userId(userId)
                .username(username)
                .action(action)
                .description(description)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .success(success)
                .details(details)
                .build();
        
        auditLogRepository.save(auditLog);
    }
    
    public void logSuccess(AuditAction action, String description, Long userId, String username) {
        log(action, description, userId, username, null, null, true, null);
    }
    
    public void logFailure(AuditAction action, String description, Long userId, String username, String details) {
        log(action, description, userId, username, null, null, false, details);
    }
    
    public void logArticleAction(AuditAction action, Long userId, String username, Long articleId, String details) {
        log(action, action.name(), userId, username, "Article", articleId, true, details);
    }
    
    public void logUserAction(AuditAction action, Long userId, String username, Long targetUserId, String details) {
        log(action, action.name(), userId, username, "User", targetUserId, true, details);
    }
    
    public Page<AuditLogResponse> getAllLogs(Pageable pageable) {
        return auditLogRepository.findAllOrderByTimestampDesc(pageable)
                .map(this::convertToResponse);
    }
    
    public Page<AuditLogResponse> getLogsByUserId(Long userId, Pageable pageable) {
        return auditLogRepository.findByUserId(userId, pageable)
                .map(this::convertToResponse);
    }
    
    public Page<AuditLogResponse> getLogsByAction(AuditAction action, Pageable pageable) {
        return auditLogRepository.findByAction(action, pageable)
                .map(this::convertToResponse);
    }
    
    public Page<AuditLogResponse> getLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return auditLogRepository.findByTimestampBetween(startDate, endDate, pageable)
                .map(this::convertToResponse);
    }
    
    private AuditLogResponse convertToResponse(AuditLog auditLog) {
        return AuditLogResponse.builder()
                .id(auditLog.getId())
                .userId(auditLog.getUserId())
                .username(auditLog.getUsername())
                .action(auditLog.getAction())
                .description(auditLog.getDescription())
                .resourceType(auditLog.getResourceType())
                .resourceId(auditLog.getResourceId())
                .ipAddress(auditLog.getIpAddress())
                .browser(auditLog.getBrowser())
                .device(auditLog.getDevice())
                .operatingSystem(auditLog.getOperatingSystem())
                .success(auditLog.getSuccess())
                .details(auditLog.getDetails())
                .timestamp(auditLog.getTimestamp())
                .build();
    }
    
    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = 
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }
}

