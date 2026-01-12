package com.lingnan.fruitshop.dto.admin.user.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class AdminUserOrderItemResponse {

    private Long orderId;
    private String orderNo;
    private Long shopId;
    private String shopName;
    private Integer status;
    private String statusText;
    private BigDecimal actualAmount;
    private String createTime;
}
