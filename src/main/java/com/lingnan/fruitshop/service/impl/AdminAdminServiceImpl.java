package com.lingnan.fruitshop.service.impl;

import com.lingnan.fruitshop.service.AdminAdminService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingnan.fruitshop.common.exception.BizException;
import com.lingnan.fruitshop.dto.admin.admin.AdminAddRequest;
import com.lingnan.fruitshop.dto.admin.admin.AdminResetPasswordRequest;
import com.lingnan.fruitshop.dto.admin.admin.AdminUpdateRequest;
import com.lingnan.fruitshop.dto.admin.admin.vo.AdminListItemResponse;
import com.lingnan.fruitshop.dto.customer.common.vo.PageResponse;
import com.lingnan.fruitshop.entity.SysAdmin;
import com.lingnan.fruitshop.entity.SysAdminRole;
import com.lingnan.fruitshop.entity.SysRole;
import com.lingnan.fruitshop.mapper.SysAdminMapper;
import com.lingnan.fruitshop.mapper.SysAdminRoleMapper;
import com.lingnan.fruitshop.mapper.SysRoleMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class AdminAdminServiceImpl implements AdminAdminService {

    // ==================== 常量定义 ====================
    private static final DateTimeFormatter DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ==================== 依赖注入区域 ====================
    // 注入管理员相关的数据访问层：管理员、角色关联、角色、密码编码器
    private final SysAdminMapper sysAdminMapper;
    private final SysAdminRoleMapper sysAdminRoleMapper;
    private final SysRoleMapper sysRoleMapper;
    private final PasswordEncoder passwordEncoder;

    public AdminAdminServiceImpl(SysAdminMapper sysAdminMapper,
                                SysAdminRoleMapper sysAdminRoleMapper,
                                SysRoleMapper sysRoleMapper,
                                PasswordEncoder passwordEncoder) {
        this.sysAdminMapper = sysAdminMapper;
        this.sysAdminRoleMapper = sysAdminRoleMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.passwordEncoder = passwordEncoder;
    }

    // ==================== 管理员列表查询核心逻辑 ====================
    // 支持按关键词、角色、状态筛选，包含角色关联查询
    @Override
    public PageResponse<AdminListItemResponse> list(int page, int pageSize, String keyword, Long roleId, Integer status) {
        // 步骤1: 如果指定了角色筛选，先查询该角色下的所有管理员ID
        Set<Long> filterAdminIds = null;
        if (roleId != null) {
            List<SysAdminRole> adminRoles = sysAdminRoleMapper.selectList(new LambdaQueryWrapper<SysAdminRole>()
                    .eq(SysAdminRole::getRoleId, roleId));
            filterAdminIds = new HashSet<>();
            for (SysAdminRole ar : adminRoles) {
                if (ar.getAdminId() != null) {
                    filterAdminIds.add(ar.getAdminId());
                }
            }
            // 如果该角色下没有管理员，直接返回空结果
            if (filterAdminIds.isEmpty()) {
                return new PageResponse<>(0, List.of());
            }
        }

        // 步骤2: 构建管理员查询条件
        LambdaQueryWrapper<SysAdmin> qw = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            // 支持按用户名或昵称模糊搜索
            qw.and(w -> w.like(SysAdmin::getUsername, keyword)
                    .or()
                    .like(SysAdmin::getNickname, keyword));
        }
        if (status != null) {
            qw.eq(SysAdmin::getStatus, status);       // 按状态筛选
        }
        if (filterAdminIds != null) {
            qw.in(SysAdmin::getAdminId, filterAdminIds); // 按角色筛选管理员ID
        }
        qw.orderByDesc(SysAdmin::getCreateTime);       // 按创建时间倒序

        // 步骤3: 执行分页查询
        Page<SysAdmin> p = new Page<>(page, pageSize);
        Page<SysAdmin> result = sysAdminMapper.selectPage(p, qw);
        if (result.getRecords().isEmpty()) {
            return new PageResponse<>(0, List.of());
        }

        Set<Long> adminIds = new HashSet<>();
        for (SysAdmin a : result.getRecords()) {
            adminIds.add(a.getAdminId());
        }

        Map<Long, SysAdminRole> adminRoleMap = new HashMap<>();
        List<SysAdminRole> roles = sysAdminRoleMapper.selectList(new LambdaQueryWrapper<SysAdminRole>()
                .in(SysAdminRole::getAdminId, adminIds));
        Set<Long> roleIds = new HashSet<>();
        for (SysAdminRole r : roles) {
            adminRoleMap.put(r.getAdminId(), r);
            if (r.getRoleId() != null) {
                roleIds.add(r.getRoleId());
            }
        }

        Map<Long, SysRole> roleMap = new HashMap<>();
        if (!roleIds.isEmpty()) {
            for (SysRole r : sysRoleMapper.selectBatchIds(roleIds)) {
                roleMap.put(r.getRoleId(), r);
            }
        }

        List<AdminListItemResponse> list = result.getRecords().stream().map(a -> {
            SysAdminRole ar = adminRoleMap.get(a.getAdminId());
            Long rid = ar == null ? null : ar.getRoleId();
            SysRole role = rid == null ? null : roleMap.get(rid);
            return new AdminListItemResponse(
                    a.getAdminId(),
                    a.getUsername(),
                    a.getNickname(),
                    a.getAvatar(),
                    a.getPhone(),
                    a.getEmail(),
                    rid,
                    role == null ? null : role.getRoleName(),
                    a.getStatus(),
                    format(a.getLastLoginTime()),
                    a.getLastLoginIp(),
                    format(a.getCreateTime())
            );
        }).toList();

        return new PageResponse<>(result.getTotal(), list);
    }

    @Override
    /**
     * 添加管理员用户方法
     * @param req 包含管理员添加信息的请求对象，包括用户名、密码、昵称、电话、邮箱、状态和角色ID等
     * @throws BizException 当用户名已存在时抛出业务异常
     */
    public void add(AdminAddRequest req) {
        // 检查用户名是否已存在
        Long cnt = sysAdminMapper.selectCount(new LambdaQueryWrapper<SysAdmin>()
                .eq(SysAdmin::getUsername, req.getUsername()));
        if (cnt != null && cnt > 0) {
            throw BizException.badRequest("用户名已存在");
        }

        SysAdmin admin = new SysAdmin();
        admin.setUsername(req.getUsername());
        admin.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        admin.setNickname(req.getNickname());
        admin.setPhone(req.getPhone());
        admin.setEmail(req.getEmail());
        admin.setStatus(req.getStatus());
        sysAdminMapper.insert(admin);

        SysAdminRole ar = new SysAdminRole();
        ar.setAdminId(admin.getAdminId());
        ar.setRoleId(req.getRoleId());
        sysAdminRoleMapper.insert(ar);
    }

    @Override
    public void update(long adminId, AdminUpdateRequest req) {
        SysAdmin admin = sysAdminMapper.selectById(adminId);
        if (admin == null) {
            throw BizException.notFound("管理员不存在");
        }
        admin.setNickname(req.getNickname());
        admin.setPhone(req.getPhone());
        admin.setEmail(req.getEmail());
        admin.setStatus(req.getStatus());
        sysAdminMapper.updateById(admin);

        SysAdminRole ar = sysAdminRoleMapper.selectOne(new LambdaQueryWrapper<SysAdminRole>()
                .eq(SysAdminRole::getAdminId, adminId)
                .last("limit 1"));
        if (ar == null) {
            ar = new SysAdminRole();
            ar.setAdminId(adminId);
            ar.setRoleId(req.getRoleId());
            sysAdminRoleMapper.insert(ar);
        } else {
            ar.setRoleId(req.getRoleId());
            sysAdminRoleMapper.updateById(ar);
        }
    }

    @Override
    public void delete(long adminId) {
        sysAdminRoleMapper.delete(new LambdaQueryWrapper<SysAdminRole>().eq(SysAdminRole::getAdminId, adminId));
        sysAdminMapper.deleteById(adminId);
    }

    @Override
    public void resetPassword(long adminId, AdminResetPasswordRequest req) {
        SysAdmin admin = sysAdminMapper.selectById(adminId);
        if (admin == null) {
            throw BizException.notFound("管理员不存在");
        }
        admin.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        sysAdminMapper.updateById(admin);
    }

    private String format(LocalDateTime dt) {
        return dt == null ? null : DATETIME.format(dt);
    }
}
