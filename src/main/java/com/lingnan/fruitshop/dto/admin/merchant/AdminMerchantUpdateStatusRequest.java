package com.lingnan.fruitshop.dto.admin.merchant;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminMerchantUpdateStatusRequest {

    @NotNull
    private Long merchantId;

    @NotNull
    private Integer status;

    private String reason;
}
