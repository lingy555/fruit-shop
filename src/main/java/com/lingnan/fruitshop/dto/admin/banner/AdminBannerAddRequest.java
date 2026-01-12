package com.lingnan.fruitshop.dto.admin.banner;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminBannerAddRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String image;

    @NotBlank
    private String position;

    @NotBlank
    private String linkType;

    private String linkValue;

    @NotNull
    private Integer sort;

    private String startTime;

    private String endTime;
}
