package com.library.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {
    
    private JwtUtil jwtUtil;
    
    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "testSecretKeyForTestingPurposesOnly123456789012345678901234567890");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 3600000L); // 1 hour
    }
    
    @Test
    void testGenerateToken() {
        UserDetails userDetails = User.builder()
                .username("testuser")
                .password("password")
                .authorities(new ArrayList<>())
                .build();
        
        String token = jwtUtil.generateToken(userDetails);
        
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }
    
    @Test
    void testExtractUsername() {
        UserDetails userDetails = User.builder()
                .username("testuser")
                .password("password")
                .authorities(new ArrayList<>())
                .build();
        
        String token = jwtUtil.generateToken(userDetails);
        String username = jwtUtil.extractUsername(token);
        
        assertEquals("testuser", username);
    }
    
    @Test
    void testValidateToken() {
        UserDetails userDetails = User.builder()
                .username("testuser")
                .password("password")
                .authorities(new ArrayList<>())
                .build();
        
        String token = jwtUtil.generateToken(userDetails);
        
        assertTrue(jwtUtil.validateToken(token, userDetails));
    }
    
    @Test
    void testValidateTokenWithWrongUsername() {
        UserDetails userDetails1 = User.builder()
                .username("testuser1")
                .password("password")
                .authorities(new ArrayList<>())
                .build();
        
        UserDetails userDetails2 = User.builder()
                .username("testuser2")
                .password("password")
                .authorities(new ArrayList<>())
                .build();
        
        String token = jwtUtil.generateToken(userDetails1);
        
        assertFalse(jwtUtil.validateToken(token, userDetails2));
    }
    
    @Test
    void testGenerateTokenWithClaims() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", 1L);
        claims.put("role", "ADMIN");
        
        String token = jwtUtil.generateToken("testuser", claims);
        
        assertNotNull(token);
        assertFalse(token.isEmpty());
        
        String username = jwtUtil.extractUsername(token);
        assertEquals("testuser", username);
    }
    
    @Test
    void testValidateTokenSimple() {
        UserDetails userDetails = User.builder()
                .username("testuser")
                .password("password")
                .authorities(new ArrayList<>())
                .build();
        
        String token = jwtUtil.generateToken(userDetails);
        
        assertTrue(jwtUtil.validateToken(token));
    }
    
    @Test
    void testValidateInvalidToken() {
        assertFalse(jwtUtil.validateToken("invalid.token.here"));
    }
}

