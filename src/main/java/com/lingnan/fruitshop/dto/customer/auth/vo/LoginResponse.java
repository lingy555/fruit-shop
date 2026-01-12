package com.lingnan.fruitshop.dto.customer.auth.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private long tokenExpire;
    private UserInfo userInfo;

    @Data
    @AllArgsConstructor
    public static class UserInfo {
        private Long userId;
        private String username;
        private String phone;
        private String avatar;
        private String nickname;
        private Integer level;
        private Integer points;
    }
}
