package com.library.controller;

import com.library.dto.request.LoginRequest;
import com.library.dto.request.RegisterRequest;
import com.library.dto.request.VerifyOtpRequest;
import com.library.dto.response.AuthResponse;
import com.library.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private AuthService authService;
    
    @Test
    void testRegister() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .fullname("Test User")
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .build();
        
        AuthResponse response = new AuthResponse("jwt-token", 1L, "testuser", "test@example.com", "VIEWER");
        
        when(authService.register(any(RegisterRequest.class))).thenReturn(response);
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("jwt-token"))
                .andExpect(jsonPath("$.data.username").value("testuser"));
    }
    
    @Test
    void testRegisterValidationError() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("ab") // Too short
                .email("invalid-email")
                .password("123") // Too short
                .build();
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testLogin() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .usernameOrEmail("testuser")
                .password("password123")
                .build();
        
        AuthResponse response = AuthResponse.builder()
                .mfaRequired(true)
                .userId(1L)
                .username("testuser")
                .message("OTP sent to your email")
                .build();
        
        when(authService.login(any(LoginRequest.class))).thenReturn(response);
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.mfaRequired").value(true));
    }
    
    @Test
    void testVerifyOtp() throws Exception {
        VerifyOtpRequest request = VerifyOtpRequest.builder()
                .usernameOrEmail("testuser")
                .otpCode("123456")
                .build();
        
        AuthResponse response = new AuthResponse("jwt-token", 1L, "testuser", "test@example.com", "VIEWER");
        
        when(authService.verifyOtp(any(VerifyOtpRequest.class))).thenReturn(response);
        
        mockMvc.perform(post("/api/auth/verify-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("jwt-token"));
    }
}

