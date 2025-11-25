package com.library.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String token;
    
    @Builder.Default
    private String type = "Bearer";
    private Long userId;
    private String username;
    private String email;
    private String role;
    private boolean mfaRequired;
    private String message;
    
    public AuthResponse(String token, Long userId, String username, String email, String role) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.role = role;
        this.mfaRequired = false;
    }
}

