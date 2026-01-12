package com.lingnan.fruitshop.dto.admin.admin.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminListItemResponse {

    private Long adminId;
    private String username;
    private String nickname;
    private String avatar;
    private String phone;
    private String email;
    private Long roleId;
    private String roleName;
    private Integer status;
    private String lastLoginTime;
    private String lastLoginIp;
    private String createTime;
}
