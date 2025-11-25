package com.library.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class DeviceUtil {
    
    public String getClientIp(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        
        // Handle multiple IPs (take the first one)
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }
        
        return ipAddress;
    }
    
    public Map<String, String> extractDeviceInfo(HttpServletRequest request) {
        Map<String, String> deviceInfo = new HashMap<>();
        String userAgent = request.getHeader("User-Agent");
        
        if (userAgent == null || userAgent.isEmpty()) {
            deviceInfo.put("browser", "Unknown");
            deviceInfo.put("device", "Unknown");
            deviceInfo.put("os", "Unknown");
            return deviceInfo;
        }
        
        // Extract browser
        String browser = extractBrowser(userAgent);
        deviceInfo.put("browser", browser);
        
        // Extract OS
        String os = extractOperatingSystem(userAgent);
        deviceInfo.put("os", os);
        
        // Extract device type
        String device = extractDevice(userAgent);
        deviceInfo.put("device", device);
        
        return deviceInfo;
    }
    
    private String extractBrowser(String userAgent) {
        if (userAgent.contains("Edg")) return "Edge";
        if (userAgent.contains("Chrome") && !userAgent.contains("Edg")) return "Chrome";
        if (userAgent.contains("Firefox")) return "Firefox";
        if (userAgent.contains("Safari") && !userAgent.contains("Chrome")) return "Safari";
        if (userAgent.contains("Opera") || userAgent.contains("OPR")) return "Opera";
        if (userAgent.contains("MSIE") || userAgent.contains("Trident")) return "Internet Explorer";
        return "Unknown";
    }
    
    private String extractOperatingSystem(String userAgent) {
        if (userAgent.contains("Windows NT 10.0")) return "Windows 10";
        if (userAgent.contains("Windows NT 11.0")) return "Windows 11";
        if (userAgent.contains("Windows NT 6.3")) return "Windows 8.1";
        if (userAgent.contains("Windows NT 6.2")) return "Windows 8";
        if (userAgent.contains("Windows NT 6.1")) return "Windows 7";
        if (userAgent.contains("Windows")) return "Windows";
        if (userAgent.contains("Mac OS X")) return "macOS";
        if (userAgent.contains("Linux")) return "Linux";
        if (userAgent.contains("Android")) return "Android";
        if (userAgent.contains("iOS") || userAgent.contains("iPhone") || userAgent.contains("iPad")) return "iOS";
        return "Unknown";
    }
    
    private String extractDevice(String userAgent) {
        if (userAgent.contains("Mobile") || userAgent.contains("Android") || 
            userAgent.contains("iPhone")) return "Mobile";
        if (userAgent.contains("Tablet") || userAgent.contains("iPad")) return "Tablet";
        return "Desktop";
    }
}

