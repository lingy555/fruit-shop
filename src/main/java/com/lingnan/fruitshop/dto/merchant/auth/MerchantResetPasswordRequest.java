package com.lingnan.fruitshop.dto.merchant.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class MerchantResetPasswordRequest {

    @NotBlank
    @Pattern(regexp = "^\\d{11}$")
    private String phone;

    @NotBlank
    private String verifyCode;

    @NotBlank
    private String newPassword;
}
