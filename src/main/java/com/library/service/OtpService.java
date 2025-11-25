package com.library.service;

import com.library.domain.entity.OtpToken;
import com.library.domain.entity.User;
import com.library.repository.OtpTokenRepository;
import com.library.util.OtpGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class OtpService {
    
    @Autowired
    private OtpTokenRepository otpTokenRepository;
    
    @Autowired
    private OtpGenerator otpGenerator;
    
    @Autowired
    private EmailService emailService;
    
    @Value("${security.otp-expiration-minutes:5}")
    private int otpExpirationMinutes;
    
    @Transactional
    public String generateAndSendOtp(User user) {
        // Delete any existing unused OTPs for this user
        otpTokenRepository.deleteByUserId(user.getId());
        
        // Generate new OTP
        String otpCode = otpGenerator.generateOtp();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(otpExpirationMinutes);
        
        // Save OTP token
        OtpToken otpToken = OtpToken.builder()
                .userId(user.getId())
                .otpCode(otpCode)
                .expiresAt(expiresAt)
                .used(false)
                .build();
        
        otpTokenRepository.save(otpToken);
        
        // Send OTP via email
        emailService.sendOtpEmail(user.getEmail(), otpCode, user.getUsername());
        
        return otpCode;
    }
    
    public boolean verifyOtp(Long userId, String otpCode) {
        OtpToken otpToken = otpTokenRepository.findValidOtpByUserIdAndCode(userId, otpCode)
                .orElse(null);
        
        if (otpToken == null) {
            return false;
        }
        
        if (otpToken.isExpired() || otpToken.getUsed()) {
            return false;
        }
        
        // Mark OTP as used
        otpToken.setUsed(true);
        otpTokenRepository.save(otpToken);
        
        return true;
    }
    
    @Transactional
    public void cleanupExpiredOtps() {
        otpTokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }
}

