package com.lingnan.fruitshop.dto.admin.banner.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminBannerItemResponse {

    private Long bannerId;
    private String title;
    private String image;
    private String position;
    private String linkType;
    private String linkValue;
    private Integer sort;
    private Integer status;
    private String startTime;
    private String endTime;
    private String createTime;
}
