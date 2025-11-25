package com.library.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Library Management System API",
        version = "1.0.0",
        description = """
            Secure Library/Article Management System with comprehensive features:
            
            **Features:**
            - JWT Authentication with MFA (Email OTP)
            - Role-Based Access Control (RBAC)
            - Failed Login Attempt Tracking & Account Locking
            - Comprehensive Audit Logging
            - Rate Limiting
            - Redis Caching
            - Article CRUD with permissions
            - User Management
            
            **Roles:**
            - VIEWER: Can only view public articles
            - CONTRIBUTOR: Can create/update own articles
            - EDITOR: Can CRUD own articles, view all
            - SUPER_ADMIN: Full access to all resources
            
            **Authentication Flow:**
            1. Register or Login with username/email + password
            2. OTP will be sent to your email
            3. Verify OTP to receive JWT token
            4. Use JWT token in Authorization header: Bearer <token>
            """,
        contact = @Contact(
            name = "Library Management System",
            email = "support@library.com"
        )
    ),
    servers = {
        @Server(url = "http://localhost:8080", description = "Local Development Server"),
        @Server(url = "https://api.library.com", description = "Production Server")
    }
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    description = "Enter JWT token obtained from login + OTP verification"
)
public class OpenApiConfig {
}

