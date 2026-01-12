package com.lingnan.fruitshop.dto.merchant.comment.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class MerchantCommentListResponse {

    private long total;
    private Summary summary;
    private List<Item> list;

    @Data
    @AllArgsConstructor
    public static class Summary {
        private double goodRate;
        private double avgScore;
        private long unreplyCount;
    }

    @Data
    @AllArgsConstructor
    public static class Item {
        private Long commentId;
        private Long orderId;
        private String orderNo;
        private Long userId;
        private String username;
        private String avatar;
        private Long productId;
        private String productName;
        private String productImage;
        private Integer score;
        private String content;
        private List<String> images;
        private String specs;
        private String createTime;
        private Reply reply;

        @JsonProperty("hasReply")
        private boolean hasReply;
    }

    @Data
    @AllArgsConstructor
    public static class Reply {
        private String content;
        private String replyTime;
    }
}
