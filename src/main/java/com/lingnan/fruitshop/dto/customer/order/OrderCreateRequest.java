package com.lingnan.fruitshop.dto.customer.order;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class OrderCreateRequest {

    @NotNull
    private Long addressId;

    @NotEmpty
    private List<Long> cartIds;

    private Long couponId;

    private Integer usePoints;

    private String remark;

    private String paymentMethod;
}
