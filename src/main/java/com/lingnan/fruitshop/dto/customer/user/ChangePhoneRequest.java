package com.lingnan.fruitshop.dto.customer.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ChangePhoneRequest {

    @NotBlank
    @Pattern(regexp = "^\\d{11}$")
    private String newPhone;

    @NotBlank
    private String verifyCode;
}
