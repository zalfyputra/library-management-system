package com.library.domain.enums;

public enum AuditAction {
    // User Actions
    USER_REGISTER,
    USER_LOGIN,
    USER_LOGOUT,
    USER_LOGIN_FAILED,
    USER_ACCOUNT_LOCKED,
    USER_OTP_SENT,
    USER_OTP_VERIFIED,
    USER_CREATED,
    USER_UPDATED,
    USER_DELETED,
    
    // Article Actions
    ARTICLE_CREATED,
    ARTICLE_UPDATED,
    ARTICLE_DELETED,
    ARTICLE_VIEWED,
    
    // System Actions
    RATE_LIMIT_EXCEEDED,
    UNAUTHORIZED_ACCESS
}

