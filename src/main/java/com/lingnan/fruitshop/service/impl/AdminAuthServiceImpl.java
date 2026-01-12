package com.lingnan.fruitshop.service.impl;

import com.lingnan.fruitshop.service.AdminAuthService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lingnan.fruitshop.common.exception.BizException;
import com.lingnan.fruitshop.dto.admin.auth.AdminChangePasswordRequest;
import com.lingnan.fruitshop.dto.admin.auth.AdminLoginRequest;
import com.lingnan.fruitshop.dto.admin.auth.AdminUpdateProfileRequest;
import com.lingnan.fruitshop.dto.admin.auth.vo.AdminLoginResponse;
import com.lingnan.fruitshop.entity.SysAdmin;
import com.lingnan.fruitshop.mapper.SysAdminMapper;
import com.lingnan.fruitshop.security.AdminPrincipal;
import com.lingnan.fruitshop.security.AdminUserDetailsService;
import com.lingnan.fruitshop.security.JwtProperties;
import com.lingnan.fruitshop.security.JwtTokenUtil;
import com.lingnan.fruitshop.service.CaptchaService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AdminAuthServiceImpl implements AdminAuthService {

    private final CaptchaService captchaService;
    private final SysAdminMapper sysAdminMapper;
    private final AdminUserDetailsService adminUserDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;
    private final JwtProperties jwtProperties;

    public AdminAuthServiceImpl(CaptchaService captchaService,
                                SysAdminMapper sysAdminMapper,
                                AdminUserDetailsService adminUserDetailsService,
                                PasswordEncoder passwordEncoder,
                                JwtTokenUtil jwtTokenUtil,
                                JwtProperties jwtProperties) {
        this.captchaService = captchaService;
        this.sysAdminMapper = sysAdminMapper;
        this.adminUserDetailsService = adminUserDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenUtil = jwtTokenUtil;
        this.jwtProperties = jwtProperties;
    }

    @Override
    public AdminLoginResponse login(AdminLoginRequest req, String ip) {
        boolean ok = captchaService.verify(req.getCaptchaKey(), req.getCaptcha());
        if (!ok) {
            throw BizException.badRequest("验证码错误");
        }

        SysAdmin admin = sysAdminMapper.selectOne(new LambdaQueryWrapper<SysAdmin>()
                .eq(SysAdmin::getUsername, req.getUsername())
                .last("limit 1"));
        if (admin == null) {
            throw BizException.badRequest("账号或密码错误");
        }
        if (admin.getStatus() == null || admin.getStatus() != 1) {
            throw BizException.forbidden("账号已禁用");
        }
        if (!passwordMatches(req.getPassword(), admin.getPasswordHash())) {
            throw BizException.badRequest("账号或密码错误");
        }

        admin.setLastLoginTime(LocalDateTime.now());
        admin.setLastLoginIp(ip);
        sysAdminMapper.updateById(admin);

        UserDetails userDetails = adminUserDetailsService.loadAdminById(admin.getAdminId());
        AdminPrincipal principal = (AdminPrincipal) userDetails;

        String token = jwtTokenUtil.generateAdminToken(admin.getAdminId(), admin.getUsername());

        return new AdminLoginResponse(token, jwtProperties.getExpireSeconds(),
                new AdminLoginResponse.AdminInfo(
                        principal.getAdminId(),
                        principal.getUsername(),
                        admin.getNickname(),
                        admin.getAvatar(),
                        principal.getRoleId(),
                        principal.getRoleName(),
                        principal.getPermissions()
                ));
    }

    @Override
    public AdminLoginResponse.AdminInfo currentInfo(long adminId) {
        UserDetails userDetails = adminUserDetailsService.loadAdminById(adminId);
        AdminPrincipal principal = (AdminPrincipal) userDetails;

        SysAdmin admin = sysAdminMapper.selectById(adminId);
        if (admin == null) {
            throw BizException.unauthorized("未登录/token过期");
        }

        return new AdminLoginResponse.AdminInfo(
                principal.getAdminId(),
                principal.getUsername(),
                admin.getNickname(),
                admin.getAvatar(),
                principal.getRoleId(),
                principal.getRoleName(),
                principal.getPermissions()
        );
    }

    @Override
    public void changePassword(long adminId, AdminChangePasswordRequest req) {
        SysAdmin admin = sysAdminMapper.selectById(adminId);
        if (admin == null) {
            throw BizException.unauthorized("未登录/token过期");
        }
        if (!passwordMatches(req.getOldPassword(), admin.getPasswordHash())) {
            throw BizException.badRequest("旧密码错误");
        }
        admin.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        sysAdminMapper.updateById(admin);
    }

    @Override
    public void updateProfile(long adminId, AdminUpdateProfileRequest req) {
        SysAdmin admin = sysAdminMapper.selectById(adminId);
        if (admin == null) {
            throw BizException.unauthorized("未登录/token过期");
        }
        admin.setNickname(req.getNickname());
        admin.setAvatar(req.getAvatar());
        admin.setPhone(req.getPhone());
        admin.setEmail(req.getEmail());
        sysAdminMapper.updateById(admin);
    }

    private boolean passwordMatches(String raw, String stored) {
        if (stored == null) {
            return false;
        }
        if (stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$")) {
            return passwordEncoder.matches(raw, stored);
        }
        return raw.equals(stored);
    }
}
