package com.lingnan.fruitshop.dto.admin.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminChangePasswordRequest {

    @NotBlank
    private String oldPassword;

    @NotBlank
    private String newPassword;
}
