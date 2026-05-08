package org.pgno20.medimart.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordUpdateDTO {
    @NotBlank(message = "Current password is required")
    private String currentPassword;

    @NotBlank(message = "New password is required")
    private String newPassword;
}
