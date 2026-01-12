package com.lingnan.fruitshop.dto.admin.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminUserAdjustPointsRequest {

    @NotNull
    private Long userId;

    @NotNull
    private Integer points;

    @NotBlank
    private String type;

    private String reason;
}
