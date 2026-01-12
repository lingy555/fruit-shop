package com.lingnan.fruitshop.dto.merchant.shop;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MerchantShopStatusRequest {

    @NotNull
    private Integer status;
}
