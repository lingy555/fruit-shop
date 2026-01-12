package com.lingnan.fruitshop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("product_hot_keyword")
public class ProductHotKeyword {

    @TableId(value = "keyword_id", type = IdType.AUTO)
    private Long keywordId;

    private String keyword;

    private Integer sort;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
