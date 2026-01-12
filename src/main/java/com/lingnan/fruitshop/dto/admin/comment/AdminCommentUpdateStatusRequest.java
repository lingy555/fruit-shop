package com.lingnan.fruitshop.dto.admin.comment;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminCommentUpdateStatusRequest {

    @NotNull
    private Long commentId;

    @NotNull
    private Integer status;
}
