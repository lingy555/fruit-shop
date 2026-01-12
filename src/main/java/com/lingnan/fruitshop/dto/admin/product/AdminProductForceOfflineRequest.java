package com.lingnan.fruitshop.dto.admin.product;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminProductForceOfflineRequest {

    @NotNull
    private Long productId;

    private String reason;
}
