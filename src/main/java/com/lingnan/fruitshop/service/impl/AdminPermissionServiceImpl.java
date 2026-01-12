package com.lingnan.fruitshop.service.impl;

import com.lingnan.fruitshop.service.AdminPermissionService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lingnan.fruitshop.dto.admin.permission.vo.PermissionTreeNodeResponse;
import com.lingnan.fruitshop.entity.SysPermission;
import com.lingnan.fruitshop.mapper.SysPermissionMapper;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AdminPermissionServiceImpl implements AdminPermissionService {

    private final SysPermissionMapper sysPermissionMapper;

    public AdminPermissionServiceImpl(SysPermissionMapper sysPermissionMapper) {
        this.sysPermissionMapper = sysPermissionMapper;
    }

    @Override
    public List<PermissionTreeNodeResponse> tree() {
        List<SysPermission> all = sysPermissionMapper.selectList(new LambdaQueryWrapper<SysPermission>()
                .orderByAsc(SysPermission::getSort)
                .orderByAsc(SysPermission::getPermissionId));

        Map<Long, PermissionTreeNodeResponse> nodeMap = new HashMap<>();
        Map<Long, List<PermissionTreeNodeResponse>> childrenMap = new HashMap<>();

        for (SysPermission p : all) {
            PermissionTreeNodeResponse node = new PermissionTreeNodeResponse();
            node.setPermissionId(p.getPermissionId());
            node.setPermissionName(p.getPermissionName());
            node.setPermissionCode(p.getPermissionCode());
            node.setType(p.getType());
            node.setIcon(p.getIcon());
            node.setSort(p.getSort());
            nodeMap.put(p.getPermissionId(), node);
            childrenMap.computeIfAbsent(p.getParentId(), k -> new ArrayList<>()).add(node);
        }

        for (SysPermission p : all) {
            PermissionTreeNodeResponse parent = nodeMap.get(p.getPermissionId());
            List<PermissionTreeNodeResponse> children = childrenMap.get(p.getPermissionId());
            if (children != null) {
                parent.getChildren().addAll(children);
            }
        }

        return childrenMap.getOrDefault(0L, List.of());
    }
}
