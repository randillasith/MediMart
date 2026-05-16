package org.pgno20.medimart.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RoleUpdateDTO {
    @NotBlank(message = "Role is required")
    private String role;
}
