package com.lingnan.fruitshop.dto.merchant.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MerchantChangePasswordRequest {

    @NotBlank
    private String oldPassword;

    @NotBlank
    private String newPassword;
}
