package org.pgno20.medimart.dto;

import jakarta.validation.constraints.NotBlank;

public class RoleUpdateDTO {
    @NotBlank(message = "Role is required")
    private String role;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
