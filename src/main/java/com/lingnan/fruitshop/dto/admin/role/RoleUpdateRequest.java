package com.lingnan.fruitshop.dto.admin.role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class RoleUpdateRequest {

    @NotBlank
    private String roleName;

    @NotBlank
    private String roleCode;

    private String description;

    @NotNull
    private List<Long> permissionIds;

    @NotNull
    private Integer status;

    @NotNull
    private Integer sort;
}
