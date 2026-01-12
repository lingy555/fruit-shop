package com.lingnan.fruitshop.dto.merchant.refund;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MerchantRefundConfirmReceiveRequest {

    @NotNull
    private Long refundId;
}
