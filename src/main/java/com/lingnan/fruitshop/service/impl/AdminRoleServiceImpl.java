package com.lingnan.fruitshop.service.impl;

import com.lingnan.fruitshop.service.AdminRoleService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingnan.fruitshop.common.exception.BizException;
import com.lingnan.fruitshop.dto.admin.role.RoleAddRequest;
import com.lingnan.fruitshop.dto.admin.role.RoleUpdateRequest;
import com.lingnan.fruitshop.dto.admin.role.vo.RoleDetailResponse;
import com.lingnan.fruitshop.dto.admin.role.vo.RoleListItemResponse;
import com.lingnan.fruitshop.dto.customer.common.vo.PageResponse;
import com.lingnan.fruitshop.entity.SysAdminRole;
import com.lingnan.fruitshop.entity.SysRole;
import com.lingnan.fruitshop.entity.SysRolePermission;
import com.lingnan.fruitshop.mapper.SysAdminRoleMapper;
import com.lingnan.fruitshop.mapper.SysRoleMapper;
import com.lingnan.fruitshop.mapper.SysRolePermissionMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class AdminRoleServiceImpl implements AdminRoleService {

    private static final DateTimeFormatter DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SysRoleMapper sysRoleMapper;
    private final SysRolePermissionMapper sysRolePermissionMapper;
    private final SysAdminRoleMapper sysAdminRoleMapper;

    public AdminRoleServiceImpl(SysRoleMapper sysRoleMapper,
                            SysRolePermissionMapper sysRolePermissionMapper,
                            SysAdminRoleMapper sysAdminRoleMapper) {
        this.sysRoleMapper = sysRoleMapper;
        this.sysRolePermissionMapper = sysRolePermissionMapper;
        this.sysAdminRoleMapper = sysAdminRoleMapper;
    }

    @Override
    public PageResponse<RoleListItemResponse> list(int page, int pageSize, String keyword) {
        LambdaQueryWrapper<SysRole> qw = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            qw.and(w -> w.like(SysRole::getRoleName, keyword)
                    .or()
                    .like(SysRole::getRoleCode, keyword));
        }
        qw.orderByAsc(SysRole::getSort).orderByDesc(SysRole::getRoleId);

        Page<SysRole> p = new Page<>(page, pageSize);
        Page<SysRole> result = sysRoleMapper.selectPage(p, qw);
        List<RoleListItemResponse> list = result.getRecords().stream().map(r -> new RoleListItemResponse(
                r.getRoleId(),
                r.getRoleName(),
                r.getRoleCode(),
                r.getDescription(),
                r.getStatus(),
                r.getSort(),
                format(r.getCreateTime())
        )).toList();

        return new PageResponse<>(result.getTotal(), list);
    }

    @Override
    public RoleDetailResponse detail(long roleId) {
        SysRole role = sysRoleMapper.selectById(roleId);
        if (role == null) {
            throw BizException.notFound("角色不存在");
        }
        List<Long> permissionIds = rolePermissionIds(roleId);
        return new RoleDetailResponse(
                role.getRoleId(),
                role.getRoleName(),
                role.getRoleCode(),
                role.getDescription(),
                role.getStatus(),
                role.getSort(),
                permissionIds
        );
    }

    @Override
    public void add(RoleAddRequest req) {
        Long exists = sysRoleMapper.selectCount(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, req.getRoleCode()));
        if (exists != null && exists > 0) {
            throw BizException.badRequest("角色编码已存在");
        }

        SysRole role = new SysRole();
        role.setRoleName(req.getRoleName());
        role.setRoleCode(req.getRoleCode());
        role.setDescription(req.getDescription());
        role.setStatus(req.getStatus());
        role.setSort(req.getSort());
        sysRoleMapper.insert(role);

        insertRolePermissions(role.getRoleId(), req.getPermissionIds());
    }

    @Override
    public void update(long roleId, RoleUpdateRequest req) {
        SysRole role = sysRoleMapper.selectById(roleId);
        if (role == null) {
            throw BizException.notFound("角色不存在");
        }

        Long exists = sysRoleMapper.selectCount(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, req.getRoleCode())
                .ne(SysRole::getRoleId, roleId));
        if (exists != null && exists > 0) {
            throw BizException.badRequest("角色编码已存在");
        }

        role.setRoleName(req.getRoleName());
        role.setRoleCode(req.getRoleCode());
        role.setDescription(req.getDescription());
        role.setStatus(req.getStatus());
        role.setSort(req.getSort());
        sysRoleMapper.updateById(role);

        sysRolePermissionMapper.delete(new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getRoleId, roleId));
        insertRolePermissions(roleId, req.getPermissionIds());
    }

    @Override
    public void delete(long roleId) {
        Long used = sysAdminRoleMapper.selectCount(new LambdaQueryWrapper<SysAdminRole>()
                .eq(SysAdminRole::getRoleId, roleId));
        if (used != null && used > 0) {
            throw BizException.badRequest("角色已被使用");
        }

        sysRolePermissionMapper.delete(new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getRoleId, roleId));
        sysRoleMapper.deleteById(roleId);
    }

    @Override
    public List<Long> permissions(long roleId) {
        return rolePermissionIds(roleId);
    }

    private List<Long> rolePermissionIds(long roleId) {
        return sysRolePermissionMapper.selectList(new LambdaQueryWrapper<SysRolePermission>()
                .eq(SysRolePermission::getRoleId, roleId)).stream().map(SysRolePermission::getPermissionId).distinct().toList();
    }

    private void insertRolePermissions(long roleId, List<Long> permissionIds) {
        if (permissionIds == null || permissionIds.isEmpty()) {
            return;
        }
        Set<Long> unique = new HashSet<>();
        for (Long pid : permissionIds) {
            if (pid != null) {
                unique.add(pid);
            }
        }
        for (Long pid : unique) {
            SysRolePermission rp = new SysRolePermission();
            rp.setRoleId(roleId);
            rp.setPermissionId(pid);
            sysRolePermissionMapper.insert(rp);
        }
    }

    private String format(LocalDateTime dt) {
        return dt == null ? null : DATETIME.format(dt);
    }
}
