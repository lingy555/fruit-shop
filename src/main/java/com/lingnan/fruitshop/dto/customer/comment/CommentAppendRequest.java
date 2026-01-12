package com.lingnan.fruitshop.dto.customer.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CommentAppendRequest {

    @NotNull
    private Long commentId;

    @NotBlank
    private String content;

    private List<String> images;
}
