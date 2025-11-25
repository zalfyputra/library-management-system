package com.library.util;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class DeviceUtilTest {
    
    @Mock
    private HttpServletRequest request;
    
    private DeviceUtil deviceUtil;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        deviceUtil = new DeviceUtil();
    }
    
    @Test
    void testGetClientIpFromXForwardedFor() {
        when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.1");
        
        String ip = deviceUtil.getClientIp(request);
        
        assertEquals("192.168.1.1", ip);
    }
    
    @Test
    void testGetClientIpFromRemoteAddr() {
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        
        String ip = deviceUtil.getClientIp(request);
        
        assertEquals("127.0.0.1", ip);
    }
    
    @Test
    void testGetClientIpMultipleIps() {
        when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.1, 10.0.0.1");
        
        String ip = deviceUtil.getClientIp(request);
        
        assertEquals("192.168.1.1", ip);
    }
    
    @Test
    void testExtractDeviceInfoChrome() {
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";
        when(request.getHeader("User-Agent")).thenReturn(userAgent);
        
        Map<String, String> deviceInfo = deviceUtil.extractDeviceInfo(request);
        
        assertEquals("Chrome", deviceInfo.get("browser"));
        assertEquals("Windows 10", deviceInfo.get("os"));
        assertEquals("Desktop", deviceInfo.get("device"));
    }
    
    @Test
    void testExtractDeviceInfoFirefox() {
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:89.0) Gecko/20100101 Firefox/89.0";
        when(request.getHeader("User-Agent")).thenReturn(userAgent);
        
        Map<String, String> deviceInfo = deviceUtil.extractDeviceInfo(request);
        
        assertEquals("Firefox", deviceInfo.get("browser"));
        assertEquals("Windows 10", deviceInfo.get("os"));
        assertEquals("Desktop", deviceInfo.get("device"));
    }
    
    @Test
    void testExtractDeviceInfoMobile() {
        String userAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 14_6 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.0 Mobile/15E148 Safari/604.1";
        when(request.getHeader("User-Agent")).thenReturn(userAgent);
        
        Map<String, String> deviceInfo = deviceUtil.extractDeviceInfo(request);
        
        assertEquals("Safari", deviceInfo.get("browser"));
        assertEquals("iOS", deviceInfo.get("os"));
        assertEquals("Mobile", deviceInfo.get("device"));
    }
    
    @Test
    void testExtractDeviceInfoNoUserAgent() {
        when(request.getHeader("User-Agent")).thenReturn(null);
        
        Map<String, String> deviceInfo = deviceUtil.extractDeviceInfo(request);
        
        assertEquals("Unknown", deviceInfo.get("browser"));
        assertEquals("Unknown", deviceInfo.get("os"));
        assertEquals("Unknown", deviceInfo.get("device"));
    }
}

