package com.lingnan.fruitshop.dto.customer.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CategoryTreeResponse {

    private Long categoryId;
    private String categoryName;
    private String icon;
    private Long parentId;
    private Integer level;
    private Integer sort;
    private List<CategoryTreeResponse> children;
    private Long productCount;
}
