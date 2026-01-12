package com.lingnan.fruitshop.dto.admin.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminAddRequest {

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    private String nickname;

    private String phone;

    private String email;

    @NotNull
    private Long roleId;

    @NotNull
    private Integer status;
}
