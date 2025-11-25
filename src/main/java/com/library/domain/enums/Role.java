package com.library.domain.enums;

public enum Role {
    VIEWER("Viewer - Can only view public articles"),
    CONTRIBUTOR("Contributor - Can create and update own articles"),
    EDITOR("Editor - Can perform CRUD on own articles and view all"),
    SUPER_ADMIN("Super Admin - Full access to all resources");
    
    private final String description;
    
    Role(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}

