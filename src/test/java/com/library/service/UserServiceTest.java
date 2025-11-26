package com.library.service;

import com.library.domain.entity.User;
import com.library.domain.enums.Role;
import com.library.dto.request.UserUpdateRequest;
import com.library.dto.response.UserResponse;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private AuditLogService auditLogService;
    
    @InjectMocks
    private UserService userService;
    
    private User user;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        user = User.builder()
                .id(1L)
                .fullname("Test User")
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .role(Role.VIEWER)
                .enabled(true)
                .accountLocked(false)
                .build();
    }
    
    @Test
    void testGetAllUsers() {
        when(userRepository.findAll()).thenReturn(Arrays.asList(user));
        
        List<UserResponse> users = userService.getAllUsers();
        
        assertNotNull(users);
        assertEquals(1, users.size());
        assertEquals("testuser", users.get(0).getUsername());
    }
    
    @Test
    void testGetUserById() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        
        UserResponse response = userService.getUserById(1L);
        
        assertNotNull(response);
        assertEquals("testuser", response.getUsername());
    }
    
    @Test
    void testGetUserByIdNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        
        assertThrows(ResourceNotFoundException.class, 
            () -> userService.getUserById(999L));
    }
    
    @Test
    void testGetUserByUsername() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        
        UserResponse response = userService.getUserByUsername("testuser");
        
        assertNotNull(response);
        assertEquals("testuser", response.getUsername());
    }
    
    @Test
    void testUpdateUser() {
        UserUpdateRequest request = UserUpdateRequest.builder()
                .fullname("Updated Name")
                .email("updated@example.com")
                .role(Role.CONTRIBUTOR)
                .build();
        
        User admin = User.builder()
                .id(2L)
                .username("admin")
                .role(Role.SUPER_ADMIN)
                .build();
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(userRepository.existsByEmail("updated@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);
        
        UserResponse response = userService.updateUser(1L, request, "admin");
        
        assertNotNull(response);
        verify(userRepository, times(1)).save(any(User.class));
        verify(auditLogService, times(1)).logUserAction(any(), anyLong(), anyString(), anyLong(), anyString());
    }
    
    @Test
    void testUpdateUserEmailAlreadyExists() {
        UserUpdateRequest request = UserUpdateRequest.builder()
                .email("existing@example.com")
                .build();
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);
        
        assertThrows(IllegalArgumentException.class, 
            () -> userService.updateUser(1L, request, "admin"));
    }
    
    @Test
    void testDeleteUser() {
        User admin = User.builder()
                .id(2L)
                .username("admin")
                .role(Role.SUPER_ADMIN)
                .build();
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        
        userService.deleteUser(1L, "admin");
        
        verify(userRepository, times(1)).delete(user);
        verify(auditLogService, times(1)).logUserAction(any(), anyLong(), anyString(), anyLong(), anyString());
    }
    
    @Test
    void testGetUserEntityByUsername() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        
        User result = userService.getUserEntityByUsername("testuser");
        
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }
}
