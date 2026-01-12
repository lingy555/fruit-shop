package com.lingnan.fruitshop.dto.merchant.finance.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class MerchantFinanceBillItemResponse {

    private Long billId;
    private Integer type;
    private String typeText;
    private BigDecimal amount;
    private String description;
    private String createTime;
}
