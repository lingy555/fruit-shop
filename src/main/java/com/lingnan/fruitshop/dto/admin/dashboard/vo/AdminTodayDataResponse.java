package com.lingnan.fruitshop.dto.admin.dashboard.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class AdminTodayDataResponse {

    private long orderCount;
    private Double orderCompareRate;

    private BigDecimal orderAmount;
    private Double amountCompareRate;

    private long visitCount;
    private Double visitCompareRate;

    private long newUserCount;
    private Double userCompareRate;
}
