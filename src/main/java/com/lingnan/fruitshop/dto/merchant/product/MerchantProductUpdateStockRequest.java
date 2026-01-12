package com.lingnan.fruitshop.dto.merchant.product;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MerchantProductUpdateStockRequest {

    @NotNull
    private Long skuId;

    @NotNull
    @Min(0)
    private Integer stock;

    @NotBlank
    private String type;
}
