package com.lingnan.fruitshop.dto.admin.permission.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PermissionTreeNodeResponse {

    private Long permissionId;
    private String permissionName;
    private String permissionCode;
    private String type;
    private String icon;
    private Integer sort;
    private List<PermissionTreeNodeResponse> children = new ArrayList<>();
}
