package com.lingnan.fruitshop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("product_search_history")
public class ProductSearchHistory {

    @TableId(value = "history_id", type = IdType.AUTO)
    private Long historyId;

    private Long userId;

    private String keyword;

    private LocalDateTime lastTime;

    private LocalDateTime createTime;
}
