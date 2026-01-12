package com.lingnan.fruitshop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("product_image")
public class ProductImage {

    @TableId(value = "image_id", type = IdType.AUTO)
    private Long imageId;

    private Long productId;

    private String imageUrl;

    private Integer sort;

    private LocalDateTime createTime;
}
