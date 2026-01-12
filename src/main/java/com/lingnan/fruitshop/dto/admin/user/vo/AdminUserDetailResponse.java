package com.lingnan.fruitshop.dto.admin.user.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class AdminUserDetailResponse {

    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
    private String phone;
    private String email;
    private Integer gender;
    private String birthday;
    private Integer level;
    private String levelName;
    private Integer points;
    private BigDecimal balance;
    private BigDecimal frozenBalance;
    private Integer status;
    private long orderCount;
    private BigDecimal totalAmount;
    private long couponCount;
    private long favoriteCount;
    private long addressCount;
    private String lastLoginTime;
    private String lastLoginIp;
    private String createTime;
}
