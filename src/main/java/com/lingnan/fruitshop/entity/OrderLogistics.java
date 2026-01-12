package com.lingnan.fruitshop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("order_logistics")
public class OrderLogistics {

    @TableId(value = "logistics_id", type = IdType.AUTO)
    private Long logisticsId;

    private Long orderId;

    private String expressCompany;

    private String expressNo;

    private String currentStatus;

    private String tracesJson;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
