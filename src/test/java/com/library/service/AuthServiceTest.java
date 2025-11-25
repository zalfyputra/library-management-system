package com.library.service;

import com.library.domain.entity.User;
import com.library.domain.enums.AuditAction;
import com.library.domain.enums.Role;
import com.library.dto.request.LoginRequest;
import com.library.dto.request.RegisterRequest;
import com.library.dto.request.VerifyOtpRequest;
import com.library.dto.response.AuthResponse;
import com.library.exception.AccountLockedException;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.UserRepository;
import com.library.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private JwtUtil jwtUtil;
    
    @Mock
    private AuthenticationManager authenticationManager;
    
    @Mock
    private OtpService otpService;
    
    @Mock
    private AuditLogService auditLogService;
    
    @Mock
    private EmailService emailService;
    
    @InjectMocks
    private AuthService authService;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(authService, "maxLoginAttempts", 5);
        ReflectionTestUtils.setField(authService, "loginAttemptWindowMinutes", 10);
        ReflectionTestUtils.setField(authService, "accountLockDurationMinutes", 30);
    }
    
    @Test
    void testRegisterSuccess() {
        RegisterRequest request = RegisterRequest.builder()
                .fullname("Test User")
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .build();
        
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        
        User savedUser = User.builder()
                .id(1L)
                .fullname("Test User")
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .role(Role.VIEWER)
                .build();
        
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtUtil.generateToken(anyString(), any())).thenReturn("jwt-token");
        
        AuthResponse response = authService.register(request);
        
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("testuser", response.getUsername());
        verify(userRepository, times(1)).save(any(User.class));
        verify(emailService, times(1)).sendWelcomeEmail(anyString(), anyString());
        verify(auditLogService, times(1)).logSuccess(eq(AuditAction.USER_REGISTER), anyString(), anyLong(), anyString());
    }
    
    @Test
    void testRegisterUsernameAlreadyExists() {
        RegisterRequest request = RegisterRequest.builder()
                .username("existinguser")
                .email("test@example.com")
                .password("password123")
                .build();
        
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);
        
        assertThrows(IllegalArgumentException.class, () -> authService.register(request));
    }
    
    @Test
    void testRegisterEmailAlreadyExists() {
        RegisterRequest request = RegisterRequest.builder()
                .username("testuser")
                .email("existing@example.com")
                .password("password123")
                .build();
        
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);
        
        assertThrows(IllegalArgumentException.class, () -> authService.register(request));
    }
    
    @Test
    void testLoginSuccess() {
        LoginRequest request = LoginRequest.builder()
                .usernameOrEmail("testuser")
                .password("password123")
                .build();
        
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .accountLocked(false)
                .role(Role.VIEWER)
                .build();
        
        when(userRepository.findByUsernameOrEmail("testuser")).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(otpService.generateAndSendOtp(any(User.class))).thenReturn("123456");
        
        AuthResponse response = authService.login(request);
        
        assertNotNull(response);
        assertTrue(response.isMfaRequired());
        verify(otpService, times(1)).generateAndSendOtp(user);
        verify(auditLogService, times(1)).logSuccess(eq(AuditAction.USER_OTP_SENT), anyString(), anyLong(), anyString());
    }
    
    @Test
    void testLoginAccountLocked() {
        LoginRequest request = LoginRequest.builder()
                .usernameOrEmail("testuser")
                .password("password123")
                .build();
        
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .accountLocked(true)
                .lockedUntil(LocalDateTime.now().plusMinutes(30))
                .build();
        
        when(userRepository.findByUsernameOrEmail("testuser")).thenReturn(Optional.of(user));
        
        assertThrows(AccountLockedException.class, () -> authService.login(request));
    }
    
    @Test
    void testLoginBadCredentials() {
        LoginRequest request = LoginRequest.builder()
                .usernameOrEmail("testuser")
                .password("wrongpassword")
                .build();
        
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .accountLocked(false)
                .failedLoginAttempts(0)
                .build();
        
        when(userRepository.findByUsernameOrEmail("testuser")).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));
        
        assertThrows(BadCredentialsException.class, () -> authService.login(request));
        verify(userRepository, times(1)).save(any(User.class));
    }
    
    @Test
    void testVerifyOtpSuccess() {
        VerifyOtpRequest request = VerifyOtpRequest.builder()
                .usernameOrEmail("testuser")
                .otpCode("123456")
                .build();
        
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .role(Role.VIEWER)
                .build();
        
        when(userRepository.findByUsernameOrEmail("testuser")).thenReturn(Optional.of(user));
        when(otpService.verifyOtp(1L, "123456")).thenReturn(true);
        when(jwtUtil.generateToken(anyString(), any())).thenReturn("jwt-token");
        
        AuthResponse response = authService.verifyOtp(request);
        
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("testuser", response.getUsername());
        verify(auditLogService, times(2)).logSuccess(any(AuditAction.class), anyString(), anyLong(), anyString());
    }
    
    @Test
    void testVerifyOtpInvalidCode() {
        VerifyOtpRequest request = VerifyOtpRequest.builder()
                .usernameOrEmail("testuser")
                .otpCode("wrong")
                .build();
        
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .build();
        
        when(userRepository.findByUsernameOrEmail("testuser")).thenReturn(Optional.of(user));
        when(otpService.verifyOtp(1L, "wrong")).thenReturn(false);
        
        assertThrows(BadCredentialsException.class, () -> authService.verifyOtp(request));
    }
    
    @Test
    void testLoginUserNotFound() {
        LoginRequest request = LoginRequest.builder()
                .usernameOrEmail("nonexistent")
                .password("password123")
                .build();
        
        when(userRepository.findByUsernameOrEmail("nonexistent")).thenReturn(Optional.empty());
        
        assertThrows(ResourceNotFoundException.class, () -> authService.login(request));
    }
}

