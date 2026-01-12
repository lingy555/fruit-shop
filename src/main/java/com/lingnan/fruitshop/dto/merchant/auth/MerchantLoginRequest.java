package com.lingnan.fruitshop.dto.merchant.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MerchantLoginRequest {

    @NotBlank
    private String account;

    @NotBlank
    private String password;
}
