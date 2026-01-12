package com.lingnan.fruitshop.dto.admin.user.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class AdminUserListItemResponse {

    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
    private String phone;
    private String email;
    private Integer gender;
    private Integer level;
    private String levelName;
    private Integer points;
    private BigDecimal balance;
    private Integer status;
    private long orderCount;
    private BigDecimal totalAmount;
    private String lastLoginTime;
    private String createTime;
}
