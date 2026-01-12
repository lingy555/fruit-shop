package com.lingnan.fruitshop.dto.merchant.finance.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class MerchantFinanceOverviewResponse {

    private BigDecimal balance;
    private BigDecimal frozenBalance;
    private BigDecimal totalIncome;
    private BigDecimal totalWithdraw;
    private BigDecimal todayIncome;
    private BigDecimal pendingSettlement;
}
