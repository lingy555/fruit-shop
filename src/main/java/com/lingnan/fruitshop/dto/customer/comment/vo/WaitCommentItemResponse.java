package com.lingnan.fruitshop.dto.customer.comment.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class WaitCommentItemResponse {

    private Long orderId;
    private Long orderItemId;
    private Long productId;
    private Long skuId;
    private String productName;
    private String image;
    private BigDecimal price;
    private String specs;
    private Integer quantity;
    private String createTime;
}
