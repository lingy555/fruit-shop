package com.lingnan.fruitshop.dto.admin.comment.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AdminCommentListResponse {

    private long total;
    private List<Item> list;

    @Data
    @AllArgsConstructor
    public static class Item {
        private Long commentId;
        private Long orderId;
        private Long userId;
        private String username;
        private Long productId;
        private String productName;
        private Long shopId;
        private String shopName;
        private Integer score;
        private String content;
        private List<String> images;
        private Integer status;
        private String createTime;
    }
}
