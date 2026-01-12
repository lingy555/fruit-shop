package com.lingnan.fruitshop.dto.customer.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ProductCommentsResponse {

    private long total;
    private Summary summary;
    private List<Item> list;

    @Data
    @AllArgsConstructor
    public static class Summary {
        private double goodRate;
        private long goodCount;
        private long mediumCount;
        private long badCount;
        private long imageCount;
    }

    @Data
    @AllArgsConstructor
    public static class Item {
        private Long commentId;
        private Long userId;
        private String username;
        private String avatar;
        private Integer score;
        private String content;
        private List<String> images;
        private String specs;
        private String createTime;
        private Reply reply;
    }

    @Data
    @AllArgsConstructor
    public static class Reply {
        private String content;
        private String replyTime;
    }
}
