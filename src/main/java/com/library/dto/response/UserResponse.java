package com.library.dto.response;

import com.library.domain.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String fullname;
    private String username;
    private String email;
    private Role role;
    private Boolean enabled;
    private Boolean accountLocked;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

