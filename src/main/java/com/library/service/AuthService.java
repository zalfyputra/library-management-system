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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private OtpService otpService;
    
    @Autowired
    private AuditLogService auditLogService;
    
    @Autowired
    private EmailService emailService;
    
    @Value("${security.max-login-attempts:5}")
    private int maxLoginAttempts;
    
    @Value("${security.login-attempt-window-minutes:10}")
    private int loginAttemptWindowMinutes;
    
    @Value("${security.account-lock-duration-minutes:30}")
    private int accountLockDurationMinutes;
    
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        
        // Create new user with VIEWER role by default
        User user = User.builder()
                .fullname(request.getFullname())
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.VIEWER)
                .enabled(true)
                .accountLocked(false)
                .failedLoginAttempts(0)
                .build();
        
        user = userRepository.save(user);
        
        // Send welcome email
        emailService.sendWelcomeEmail(user.getEmail(), user.getUsername());
        
        // Log registration
        auditLogService.logSuccess(AuditAction.USER_REGISTER, 
            "User registered successfully", user.getId(), user.getUsername());
        
        // Generate JWT token
        String token = jwtUtil.generateToken(user.getUsername(), createClaims(user));
        
        return new AuthResponse(token, user.getId(), user.getUsername(), 
                               user.getEmail(), user.getRole().name());
    }
    
    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsernameOrEmail(request.getUsernameOrEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Check if account is locked
        if (user.getAccountLocked() && !user.isAccountNonLocked()) {
            auditLogService.logFailure(AuditAction.USER_LOGIN_FAILED, 
                "Login failed - Account locked", user.getId(), user.getUsername(), 
                "Account is locked until " + user.getLockedUntil());
            
            throw new AccountLockedException(
                "Account is locked due to too many failed login attempts. Try again after " + 
                user.getLockedUntil(), user.getLockedUntil());
        }
        
        try {
            // Authenticate user
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    user.getUsername(), 
                    request.getPassword()
                )
            );
            
            // Reset failed login attempts on successful authentication
            resetFailedLoginAttempts(user);
            
            // Generate and send OTP for MFA
            otpService.generateAndSendOtp(user);
            
            // Log OTP sent
            auditLogService.logSuccess(AuditAction.USER_OTP_SENT, 
                "OTP sent for MFA", user.getId(), user.getUsername());
            
            // Return response indicating MFA is required
            return AuthResponse.builder()
                    .mfaRequired(true)
                    .userId(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .message("OTP has been sent to your email. Please verify to complete login.")
                    .build();
            
        } catch (BadCredentialsException e) {
            handleFailedLoginAttempt(user);
            
            auditLogService.logFailure(AuditAction.USER_LOGIN_FAILED, 
                "Login failed - Invalid credentials", user.getId(), user.getUsername(), 
                "Attempt " + user.getFailedLoginAttempts() + "/" + maxLoginAttempts);
            
            throw new BadCredentialsException("Invalid username or password");
        }
    }
    
    @Transactional
    public AuthResponse verifyOtp(VerifyOtpRequest request) {
        User user = userRepository.findByUsernameOrEmail(request.getUsernameOrEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Verify OTP
        boolean isValid = otpService.verifyOtp(user.getId(), request.getOtpCode());
        
        if (!isValid) {
            auditLogService.logFailure(AuditAction.USER_OTP_VERIFIED, 
                "OTP verification failed", user.getId(), user.getUsername(), 
                "Invalid or expired OTP");
            throw new BadCredentialsException("Invalid or expired OTP");
        }
        
        // Log successful OTP verification
        auditLogService.logSuccess(AuditAction.USER_OTP_VERIFIED, 
            "OTP verified successfully", user.getId(), user.getUsername());
        
        // Log successful login
        auditLogService.logSuccess(AuditAction.USER_LOGIN, 
            "User logged in successfully", user.getId(), user.getUsername());
        
        // Generate JWT token
        String token = jwtUtil.generateToken(user.getUsername(), createClaims(user));
        
        return new AuthResponse(token, user.getId(), user.getUsername(), 
                               user.getEmail(), user.getRole().name());
    }
    
    @Transactional
    protected void handleFailedLoginAttempt(User user) {
        int attempts = user.getFailedLoginAttempts() == null ? 0 : user.getFailedLoginAttempts();
        LocalDateTime now = LocalDateTime.now();
        
        // Check if we should reset the counter (outside the time window)
        if (user.getLastFailedLoginAttempt() != null && 
            user.getLastFailedLoginAttempt().plusMinutes(loginAttemptWindowMinutes).isBefore(now)) {
            attempts = 0;
        }
        
        attempts++;
        user.setFailedLoginAttempts(attempts);
        user.setLastFailedLoginAttempt(now);
        
        // Lock account if max attempts exceeded
        if (attempts >= maxLoginAttempts) {
            user.setAccountLocked(true);
            user.setLockedUntil(now.plusMinutes(accountLockDurationMinutes));
            
            auditLogService.logSuccess(AuditAction.USER_ACCOUNT_LOCKED, 
                "Account locked due to too many failed login attempts", 
                user.getId(), user.getUsername());
        }
        
        userRepository.save(user);
    }
    
    @Transactional
    protected void resetFailedLoginAttempts(User user) {
        if (user.getFailedLoginAttempts() != null && user.getFailedLoginAttempts() > 0) {
            user.setFailedLoginAttempts(0);
            user.setLastFailedLoginAttempt(null);
            user.setAccountLocked(false);
            user.setLockedUntil(null);
            userRepository.save(user);
        }
    }
    
    private Map<String, Object> createClaims(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("email", user.getEmail());
        claims.put("role", user.getRole().name());
        return claims;
    }
}

