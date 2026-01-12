package com.lingnan.fruitshop.dto.admin.notice;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminNoticeUpdateRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String content;

    @NotNull
    private Integer type;

    @NotNull
    private Integer status;

    @NotNull
    private Integer sort;
}
