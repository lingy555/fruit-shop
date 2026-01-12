package com.lingnan.fruitshop.dto.customer.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank
    @Size(min = 4, max = 20)
    private String username;

    @NotBlank
    @Size(min = 6, max = 20)
    private String password;

    @NotBlank
    @Pattern(regexp = "^\\d{11}$")
    private String phone;

    private String email;

    @NotBlank
    private String verifyCode;
}
