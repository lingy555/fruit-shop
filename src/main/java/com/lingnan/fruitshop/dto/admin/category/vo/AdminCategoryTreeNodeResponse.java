package com.lingnan.fruitshop.dto.admin.category.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AdminCategoryTreeNodeResponse {

    private Long categoryId;
    private String categoryName;
    private String icon;
    private Long parentId;
    private Integer level;
    private Integer sort;
    private Integer status;
    private Long productCount;
    private List<AdminCategoryTreeNodeResponse> children;
}
