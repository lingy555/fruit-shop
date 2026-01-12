package com.lingnan.fruitshop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("cart_item")
public class CartItem {

    @TableId(value = "cart_id", type = IdType.AUTO)
    private Long cartId;

    private Long userId;

    private Long shopId;

    private Long productId;

    private Long skuId;

    private Integer quantity;

    private Integer selected;

    private Integer isValid;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
