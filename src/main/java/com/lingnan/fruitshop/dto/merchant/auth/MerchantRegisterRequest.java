package com.lingnan.fruitshop.dto.merchant.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class MerchantRegisterRequest {

    @NotBlank
    private String shopName;

    @NotBlank
    private String contactName;

    @NotBlank
    @Pattern(regexp = "^\\d{11}$")
    private String contactPhone;

    private String email;

    private String businessLicense;

    private String idCardFront;

    private String idCardBack;

    @NotBlank
    private String password;

    @NotBlank
    private String verifyCode;
}
