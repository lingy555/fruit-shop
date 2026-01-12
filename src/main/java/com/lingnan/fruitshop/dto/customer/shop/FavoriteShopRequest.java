package com.lingnan.fruitshop.dto.customer.shop;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FavoriteShopRequest {

    @NotNull
    private Long shopId;
}
