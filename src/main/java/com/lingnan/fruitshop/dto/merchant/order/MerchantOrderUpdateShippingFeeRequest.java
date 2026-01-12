package com.lingnan.fruitshop.dto.merchant.order;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MerchantOrderUpdateShippingFeeRequest {

    @NotNull
    private Long orderId;

    @NotNull
    private BigDecimal shippingFee;
}
