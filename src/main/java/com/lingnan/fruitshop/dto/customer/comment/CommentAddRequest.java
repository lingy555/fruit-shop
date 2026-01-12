package com.lingnan.fruitshop.dto.customer.comment;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CommentAddRequest {

    @NotNull
    private Long orderItemId;

    @NotNull
    private Long productId;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer score;

    @NotBlank
    private String content;

    private List<String> images;

    @NotNull
    private Boolean isAnonymous;
}
