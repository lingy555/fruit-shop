package com.lingnan.fruitshop.dto.admin.refund;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminRefundArbitrateRequest {

    @NotNull
    private Long refundId;

    @NotNull
    private Integer result;

    private String reason;
}
