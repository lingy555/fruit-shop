package com.lingnan.fruitshop.dto.customer.user;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RechargeRequest {

    @NotNull
    @DecimalMin(value = "0.01", message = "充值金额需大于0")
    private BigDecimal amount;

    /**
     * 预留支付方式（支付宝/微信等），余额直充可为空
     */
    private String paymentMethod;
}
