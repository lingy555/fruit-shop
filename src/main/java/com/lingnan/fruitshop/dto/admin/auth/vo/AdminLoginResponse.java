package com.lingnan.fruitshop.dto.admin.auth.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AdminLoginResponse {

    private String token;
    private long tokenExpire;
    private AdminInfo adminInfo;

    @Data
    @AllArgsConstructor
    public static class AdminInfo {
        private Long adminId;
        private String username;
        private String nickname;
        private String avatar;
        private Long roleId;
        private String roleName;
        private List<String> permissions;
    }
}
