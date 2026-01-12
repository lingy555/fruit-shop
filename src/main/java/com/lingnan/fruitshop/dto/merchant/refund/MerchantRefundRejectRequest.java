package com.lingnan.fruitshop.dto.merchant.refund;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class MerchantRefundRejectRequest {

    @NotNull
    private Long refundId;

    @NotBlank
    private String rejectReason;

    private List<String> images;
}
