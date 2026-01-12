package com.lingnan.fruitshop.dto.customer.user.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PointsRecordsResponse {

    private long total;
    private int currentPoints;
    private List<Item> list;

    @Data
    @AllArgsConstructor
    public static class Item {
        private Long recordId;
        private Integer type;
        private String typeName;
        private Integer points;
        private String description;
        private String createTime;
    }
}
