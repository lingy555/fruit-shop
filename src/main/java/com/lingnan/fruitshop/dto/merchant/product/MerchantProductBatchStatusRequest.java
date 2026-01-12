package com.lingnan.fruitshop.dto.merchant.product;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class MerchantProductBatchStatusRequest {

    @NotEmpty
    private List<Long> productIds;

    @NotNull
    private Integer status;
}
