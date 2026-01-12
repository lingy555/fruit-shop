package com.lingnan.fruitshop.dto.merchant.refund;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MerchantRefundAgreeRequest {

    @NotNull
    private Long refundId;

    @NotBlank
    private String merchantRemark;
}
