package com.lingnan.fruitshop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_points_record")
public class UserPointsRecord {

    @TableId(value = "record_id", type = IdType.AUTO)
    private Long recordId;

    private Long userId;

    private Integer type;

    private Integer points;

    private String description;

    private LocalDateTime createTime;
}
