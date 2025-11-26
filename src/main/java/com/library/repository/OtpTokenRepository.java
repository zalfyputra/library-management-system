package com.library.repository;

import com.library.domain.entity.OtpToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {
    
    @Query("SELECT o FROM OtpToken o WHERE o.userId = :userId AND o.otpCode = :otpCode AND o.used = false ORDER BY o.createdAt DESC")
    Optional<OtpToken> findValidOtpByUserIdAndCode(Long userId, String otpCode);
    
    @Query("SELECT o FROM OtpToken o WHERE o.userId = :userId AND o.used = false AND o.expiresAt > :now ORDER BY o.createdAt DESC")
    Optional<OtpToken> findLatestValidOtpByUserId(Long userId, LocalDateTime now);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM OtpToken o WHERE o.expiresAt < :now")
    void deleteExpiredTokens(LocalDateTime now);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM OtpToken o WHERE o.userId = :userId")
    void deleteByUserId(Long userId);
}
