package com.lingnan.fruitshop.dto.admin.notice.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminNoticeListItemResponse {

    private Long noticeId;
    private String title;
    private Integer type;
    private Integer status;
    private Integer sort;
    private String createTime;
}
