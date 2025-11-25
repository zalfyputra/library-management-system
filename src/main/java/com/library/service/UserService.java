package com.library.service;

import com.library.domain.entity.User;
import com.library.domain.enums.AuditAction;
import com.library.dto.request.UserUpdateRequest;
import com.library.dto.response.UserResponse;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private AuditLogService auditLogService;
    
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return convertToResponse(user);
    }
    
    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        return convertToResponse(user);
    }
    
    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request, String currentUsername) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        
        boolean updated = false;
        StringBuilder changes = new StringBuilder();
        
        if (request.getFullname() != null && !request.getFullname().equals(user.getFullname())) {
            user.setFullname(request.getFullname());
            changes.append("fullname, ");
            updated = true;
        }
        
        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new IllegalArgumentException("Username already exists");
            }
            user.setUsername(request.getUsername());
            changes.append("username, ");
            updated = true;
        }
        
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Email already exists");
            }
            user.setEmail(request.getEmail());
            changes.append("email, ");
            updated = true;
        }
        
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            changes.append("password, ");
            updated = true;
        }
        
        if (request.getRole() != null && !request.getRole().equals(user.getRole())) {
            user.setRole(request.getRole());
            changes.append("role, ");
            updated = true;
        }
        
        if (request.getEnabled() != null && !request.getEnabled().equals(user.getEnabled())) {
            user.setEnabled(request.getEnabled());
            changes.append("enabled, ");
            updated = true;
        }
        
        if (updated) {
            user = userRepository.save(user);
            
            // Get current user for audit
            User currentUser = userRepository.findByUsername(currentUsername).orElse(null);
            if (currentUser != null) {
                auditLogService.logUserAction(AuditAction.USER_UPDATED, 
                    currentUser.getId(), currentUser.getUsername(), user.getId(),
                    "Updated fields: " + changes.toString());
            }
        }
        
        return convertToResponse(user);
    }
    
    @Transactional
    public void deleteUser(Long id, String currentUsername) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        
        // Get current user for audit
        User currentUser = userRepository.findByUsername(currentUsername).orElse(null);
        
        userRepository.delete(user);
        
        if (currentUser != null) {
            auditLogService.logUserAction(AuditAction.USER_DELETED, 
                currentUser.getId(), currentUser.getUsername(), id,
                "Deleted user: " + user.getUsername());
        }
    }
    
    public User getUserEntityByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }
    
    private UserResponse convertToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullname(user.getFullname())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .enabled(user.getEnabled())
                .accountLocked(user.getAccountLocked())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}

