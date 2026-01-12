package com.lingnan.fruitshop.dto.admin.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminCategoryUpdateRequest {

    @NotBlank
    private String categoryName;

    private String icon;

    @NotNull
    private Long parentId;

    @NotNull
    private Integer level;

    @NotNull
    private Integer sort;

    @NotNull
    private Integer status;
}
