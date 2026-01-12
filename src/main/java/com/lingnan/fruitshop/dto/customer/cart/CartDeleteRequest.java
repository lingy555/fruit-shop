package com.lingnan.fruitshop.dto.customer.cart;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CartDeleteRequest {

    @NotEmpty
    private List<Long> cartIds;
}
