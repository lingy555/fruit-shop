package com.lingnan.fruitshop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("product")
public class Product {

    @TableId(value = "product_id", type = IdType.AUTO)
    private Long productId;

    private Long shopId;

    private String productName;

    private Long categoryId;

    private String image;

    private String videoUrl;

    private BigDecimal price;

    private BigDecimal originalPrice;

    private BigDecimal costPrice;

    private Integer stock;

    private Long sales;

    private String unit;

    private String weight;

    private String description;

    private String detail;

    private String tagsJson;

    private Integer freeShipping;

    private BigDecimal shippingFee;

    private BigDecimal score;

    private Long commentCount;

    private Long viewCount;

    private Integer status;

    private String auditRemark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
