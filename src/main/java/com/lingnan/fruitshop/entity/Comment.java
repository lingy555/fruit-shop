package com.lingnan.fruitshop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("comment")
public class Comment {

    @TableId(value = "comment_id", type = IdType.AUTO)
    private Long commentId;

    private Long orderId;

    private Long orderItemId;

    private Long userId;

    private Long productId;

    private Long shopId;

    private Integer score;

    private String content;

    private String imagesJson;

    private String specs;

    private Integer isAnonymous;

    private Integer status;

    private String merchantReplyContent;

    private LocalDateTime merchantReplyTime;

    private String appendContent;

    private String appendImagesJson;

    private LocalDateTime appendTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
