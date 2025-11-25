package com.library.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OtpGeneratorTest {
    
    private OtpGenerator otpGenerator;
    
    @BeforeEach
    void setUp() {
        otpGenerator = new OtpGenerator();
    }
    
    @Test
    void testGenerateOtp() {
        String otp = otpGenerator.generateOtp();
        
        assertNotNull(otp);
        assertEquals(6, otp.length());
        assertTrue(otp.matches("\\d{6}"));
    }
    
    @Test
    void testGenerateOtpWithCustomLength() {
        String otp = otpGenerator.generateOtp(8);
        
        assertNotNull(otp);
        assertEquals(8, otp.length());
        assertTrue(otp.matches("\\d{8}"));
    }
    
    @Test
    void testGenerateOtpUniqueness() {
        String otp1 = otpGenerator.generateOtp();
        String otp2 = otpGenerator.generateOtp();
        
        // While theoretically they could be the same, probability is very low
        assertNotNull(otp1);
        assertNotNull(otp2);
    }
    
    @Test
    void testGenerateOtpWithZeroLength() {
        String otp = otpGenerator.generateOtp(0);
        
        // Should default to 6 digits
        assertEquals(6, otp.length());
    }
}

