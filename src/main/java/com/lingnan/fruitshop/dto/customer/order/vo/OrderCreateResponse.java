package com.lingnan.fruitshop.dto.customer.order.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class OrderCreateResponse {

    private Long orderId;
    private String orderNo;
    private BigDecimal actualAmount;
    private String paymentUrl;
}
