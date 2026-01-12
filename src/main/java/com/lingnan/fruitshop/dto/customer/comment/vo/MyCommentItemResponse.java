package com.lingnan.fruitshop.dto.customer.comment.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class MyCommentItemResponse {

    private Long commentId;
    private Long orderId;
    private Long orderItemId;
    private Long productId;
    private Long shopId;
    private Integer score;
    private String content;
    private List<String> images;
    private String specs;

    @JsonProperty("isAnonymous")
    private boolean isAnonymous;

    private String createTime;
    private String merchantReplyContent;
    private String merchantReplyTime;
    private String appendContent;
    private List<String> appendImages;
    private String appendTime;
}
