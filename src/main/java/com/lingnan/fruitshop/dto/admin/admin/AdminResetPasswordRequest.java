package com.lingnan.fruitshop.dto.admin.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminResetPasswordRequest {

    @NotBlank
    private String newPassword;
}
