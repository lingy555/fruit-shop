package com.lingnan.fruitshop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("shop_finance_bill")
public class ShopFinanceBill {

    @TableId(value = "bill_id", type = IdType.AUTO)
    private Long billId;

    private Long shopId;

    private Integer type;

    private BigDecimal amount;

    private String description;

    private LocalDateTime createTime;
}
