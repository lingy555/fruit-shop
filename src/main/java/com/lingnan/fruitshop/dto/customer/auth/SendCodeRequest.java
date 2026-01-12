package com.lingnan.fruitshop.dto.customer.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SendCodeRequest {

    @NotBlank
    @Pattern(regexp = "^\\d{11}$")
    private String phone;

    @NotBlank
    private String type;
}
