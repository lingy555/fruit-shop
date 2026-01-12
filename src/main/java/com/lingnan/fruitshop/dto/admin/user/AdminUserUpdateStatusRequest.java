package com.lingnan.fruitshop.dto.admin.user;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminUserUpdateStatusRequest {

    @NotNull
    private Long userId;

    @NotNull
    private Integer status;

    private String reason;
}
