package com.lingnan.fruitshop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("product_sku")
public class ProductSku {

    @TableId(value = "sku_id", type = IdType.AUTO)
    private Long skuId;

    private Long productId;

    private String skuCode;

    private BigDecimal price;

    private Integer stock;

    private String image;

    private String specsJson;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
