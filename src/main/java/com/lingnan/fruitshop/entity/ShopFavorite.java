package com.lingnan.fruitshop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("shop_favorite")
public class ShopFavorite {

    @TableId(value = "favorite_id", type = IdType.AUTO)
    private Long favoriteId;

    private Long userId;

    private Long shopId;

    private LocalDateTime createTime;
}
