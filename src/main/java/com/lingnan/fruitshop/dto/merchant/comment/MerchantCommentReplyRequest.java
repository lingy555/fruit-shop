package com.lingnan.fruitshop.dto.merchant.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MerchantCommentReplyRequest {

    @NotNull
    private Long commentId;

    @NotBlank
    private String content;
}
