package com.lingnan.fruitshop.dto.customer.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {

    @NotBlank
    @Pattern(regexp = "^\\d{11}$")
    private String phone;

    @NotBlank
    private String verifyCode;

    @NotBlank
    @Size(min = 6, max = 20)
    private String newPassword;
}
