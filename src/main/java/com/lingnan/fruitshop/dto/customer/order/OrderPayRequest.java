package com.lingnan.fruitshop.dto.customer.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderPayRequest {

    @NotNull
    private Long orderId;

    @NotBlank
    private String paymentMethod;
}
