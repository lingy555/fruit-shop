package com.lingnan.fruitshop.dto.admin.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdminUserAdjustBalanceRequest {

    @NotNull
    private Long userId;

    @NotNull
    private BigDecimal amount;

    @NotBlank
    private String type;

    private String reason;
}
