package com.lingnan.fruitshop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("order_item")
public class OrderItem {

    @TableId(value = "order_item_id", type = IdType.AUTO)
    private Long orderItemId;

    private Long orderId;

    private Long productId;

    private Long skuId;

    private String productName;

    private String image;

    private BigDecimal price;

    private BigDecimal costPrice;

    private String specs;

    private Integer quantity;

    private BigDecimal subtotal;

    private Integer canComment;

    private LocalDateTime createTime;
}
