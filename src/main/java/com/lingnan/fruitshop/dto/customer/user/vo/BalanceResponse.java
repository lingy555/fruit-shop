package com.lingnan.fruitshop.dto.customer.user.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class BalanceResponse {

    private BigDecimal balance;
    private BigDecimal frozenBalance;
}
