package com.lingnan.fruitshop.dto.customer.user.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class UserProfileResponse {

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
    private Integer couponCount;
    private String createTime;
}
