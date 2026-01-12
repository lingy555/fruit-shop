package com.lingnan.fruitshop.dto.admin.product;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminProductAuditRequest {

    @NotNull
    private Long productId;

    @NotNull
    private Integer status;

    private String auditRemark;
}
