package com.lingnan.fruitshop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("banner")
public class Banner {

    @TableId(value = "banner_id", type = IdType.AUTO)
    private Long bannerId;

    private String title;

    private String image;

    private String position;

    private String linkType;

    private String linkValue;

    private Integer sort;

    private Integer status;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
