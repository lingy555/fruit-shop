package com.lingnan.fruitshop.dto.admin.admin;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminUpdateRequest {

    private String nickname;

    private String phone;

    private String email;

    @NotNull
    private Long roleId;

    @NotNull
    private Integer status;
}
