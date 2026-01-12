package com.lingnan.fruitshop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("shop")
public class Shop {

    @TableId(value = "shop_id", type = IdType.AUTO)
    private Long shopId;

    private Long merchantId;

    private String shopName;

    private String logo;

    private String banner;

    private String description;

    private String province;

    private String city;

    private String district;

    private String address;

    private String contactName;

    private String contactPhone;

    private String businessHours;

    private String businessLicense;

    private Integer status;

    private BigDecimal score;

    private BigDecimal totalSalesAmount;

    private Long totalSalesCount;

    private Long fanCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
