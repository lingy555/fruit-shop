package com.lingnan.fruitshop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("product_category")
public class ProductCategory {

    @TableId(value = "category_id", type = IdType.AUTO)
    private Long categoryId;

    private String categoryName;

    private String icon;

    private Long parentId;

    private Integer level;

    private Integer sort;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
