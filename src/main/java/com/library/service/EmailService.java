package com.library.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    public void sendOtpEmail(String toEmail, String otpCode, String username) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Your Login OTP Code");
            message.setText(buildOtpEmailContent(username, otpCode));
            
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send OTP email: " + e.getMessage());
        }
    }
    
    private String buildOtpEmailContent(String username, String otpCode) {
        return String.format(
            "Hello %s,\n\n" +
            "Your One-Time Password (OTP) for login is: %s\n\n" +
            "This OTP will expire in 5 minutes.\n\n" +
            "If you did not request this, please ignore this email.\n\n" +
            "Best regards,\n" +
            "Library Management System",
            username, otpCode
        );
    }
    
    public void sendWelcomeEmail(String toEmail, String username) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Welcome to Library Management System");
            message.setText(buildWelcomeEmailContent(username));
            
            mailSender.send(message);
        } catch (Exception e) {
            // Log error but don't fail registration
            System.err.println("Failed to send welcome email: " + e.getMessage());
        }
    }
    
    private String buildWelcomeEmailContent(String username) {
        return String.format(
            "Hello %s,\n\n" +
            "Welcome to the Library Management System!\n\n" +
            "Your account has been successfully created.\n\n" +
            "Best regards,\n" +
            "Library Management System",
            username
        );
    }
}

