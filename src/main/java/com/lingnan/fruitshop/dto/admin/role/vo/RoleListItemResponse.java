package com.lingnan.fruitshop.dto.admin.role.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RoleListItemResponse {

    private Long roleId;
    private String roleName;
    private String roleCode;
    private String description;
    private Integer status;
    private Integer sort;
    private String createTime;
}
