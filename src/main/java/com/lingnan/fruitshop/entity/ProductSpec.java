package com.lingnan.fruitshop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("product_spec")
public class ProductSpec {

    @TableId(value = "spec_id", type = IdType.AUTO)
    private Long specId;

    private Long productId;

    private String specName;

    private String optionsJson;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
