package com.lingnan.fruitshop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("notice")
public class Notice {

    @TableId(value = "notice_id", type = IdType.AUTO)
    private Long noticeId;

    private String title;

    private String content;

    private Integer type;

    private Integer status;

    private Integer sort;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
