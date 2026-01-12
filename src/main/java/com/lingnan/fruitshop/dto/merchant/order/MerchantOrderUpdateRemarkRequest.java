package com.lingnan.fruitshop.dto.merchant.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MerchantOrderUpdateRemarkRequest {

    @NotNull
    private Long orderId;

    @NotBlank
    private String merchantRemark;
}
