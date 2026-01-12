package com.lingnan.fruitshop.service.impl;

import com.lingnan.fruitshop.service.AuthService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lingnan.fruitshop.common.exception.BizException;
import com.lingnan.fruitshop.dto.customer.auth.*;
import com.lingnan.fruitshop.dto.customer.auth.vo.LoginResponse;
import com.lingnan.fruitshop.dto.customer.auth.vo.RegisterResponse;
import com.lingnan.fruitshop.entity.UserAccount;
import com.lingnan.fruitshop.mapper.UserAccountMapper;
import com.lingnan.fruitshop.security.JwtProperties;
import com.lingnan.fruitshop.security.JwtTokenUtil;
import com.lingnan.fruitshop.service.SmsCodeService;
import com.lingnan.fruitshop.service.CaptchaService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class AuthServiceImpl implements AuthService {

    // ==================== 依赖注入区域 ====================
    // 注入用户数据访问层、密码编码器、短信服务、JWT工具等核心组件
    private final UserAccountMapper userAccountMapper;
    private final PasswordEncoder passwordEncoder;
    private final SmsCodeService smsCodeService;
    private final CaptchaService captchaService;
    private final JwtTokenUtil jwtTokenUtil;
    private final JwtProperties jwtProperties;

    public AuthServiceImpl(UserAccountMapper userAccountMapper,
                       PasswordEncoder passwordEncoder,
                       SmsCodeService smsCodeService,
                       CaptchaService captchaService,
                       JwtTokenUtil jwtTokenUtil,
                       JwtProperties jwtProperties) {
        this.userAccountMapper = userAccountMapper;
        this.passwordEncoder = passwordEncoder;
        this.smsCodeService = smsCodeService;
        this.captchaService = captchaService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.jwtProperties = jwtProperties;
    }

    // ==================== 用户注册核心逻辑 ====================
    @Override
    public RegisterResponse register(RegisterRequest req) {
        // 步骤1: 验证短信验证码，防止恶意注册
        boolean ok = smsCodeService.verifyCode(req.getPhone(), "register", req.getVerifyCode());
        if (!ok) {
            throw BizException.badRequest("验证码错误");
        }

        // 步骤2: 检查用户名和手机号是否已存在，确保唯一性
        Long exists = userAccountMapper.selectCount(new LambdaQueryWrapper<UserAccount>()
                .eq(UserAccount::getUsername, req.getUsername())
                .or()
                .eq(UserAccount::getPhone, req.getPhone()));
        if (exists != null && exists > 0) {
            throw BizException.badRequest("用户名或手机号已存在");
        }

        // 步骤3: 创建新用户实体并设置默认值
        UserAccount user = new UserAccount();
        user.setUsername(req.getUsername());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword())); // 密码加密存储
        user.setPhone(req.getPhone());
        user.setEmail(req.getEmail());
        user.setLevel(1);           // 默认用户等级
        user.setPoints(0);          // 初始积分
        user.setBalance(BigDecimal.ZERO);        // 初始余额
        user.setFrozenBalance(BigDecimal.ZERO);  // 冻结余额
        user.setStatus(1);          // 账户状态：1-正常

        // 步骤4: 保存用户到数据库并返回注册结果
        userAccountMapper.insert(user);
        return new RegisterResponse(user.getUserId(), user.getUsername(), user.getPhone());
    }

    // ==================== 用户登录核心逻辑 ====================
    @Override
    public LoginResponse login(LoginRequest req) {
        boolean captchaOk = captchaService.verify(req.getCaptchaKey(), req.getCaptcha());
        if (!captchaOk) {
            throw BizException.badRequest("验证码错误");
        }
        // 步骤1: 根据用户名或手机号查找用户
        UserAccount user = userAccountMapper.selectOne(new LambdaQueryWrapper<UserAccount>()
                .eq(UserAccount::getUsername, req.getUsername())
                .or()
                .eq(UserAccount::getPhone, req.getUsername())
                .last("limit 1"));
        if (user == null) {
            throw BizException.badRequest("用户名或密码错误");
        }

        // 步骤2: 验证密码是否正确
        if (!passwordMatches(req.getPassword(), user.getPasswordHash())) {
            throw BizException.badRequest("用户名或密码错误");
        }

        // 步骤3: 生成JWT令牌并返回登录响应
        String token = jwtTokenUtil.generateToken(user.getUserId(), user.getUsername());
        return new LoginResponse(token, jwtProperties.getExpireSeconds(),
                new LoginResponse.UserInfo(user.getUserId(), user.getUsername(), user.getPhone(), user.getAvatar(),
                        user.getNickname(), user.getLevel(), user.getPoints()));
    }

    // ==================== 手机号登录核心逻辑 ====================
    @Override
    public LoginResponse loginByPhone(LoginByPhoneRequest req) {
        // 步骤1: 验证短信验证码
        boolean ok = smsCodeService.verifyCode(req.getPhone(), "login", req.getVerifyCode());
        if (!ok) {
            throw BizException.badRequest("验证码错误");
        }

        // 步骤2: 根据手机号查找用户
        UserAccount user = userAccountMapper.selectOne(new LambdaQueryWrapper<UserAccount>()
                .eq(UserAccount::getPhone, req.getPhone())
                .last("limit 1"));
        if (user == null) {
            throw BizException.badRequest("用户不存在");
        }

        // 步骤3: 生成JWT令牌（无需密码验证）
        String token = jwtTokenUtil.generateToken(user.getUserId(), user.getUsername());
        return new LoginResponse(token, jwtProperties.getExpireSeconds(),
                new LoginResponse.UserInfo(user.getUserId(), user.getUsername(), user.getPhone(), user.getAvatar(),
                        user.getNickname(), user.getLevel(), user.getPoints()));
    }

    // ==================== 短信验证码发送 ====================
    @Override
    public void sendCode(SendCodeRequest req) {
        // 委托给短信服务发送验证码
        smsCodeService.sendCode(req.getPhone(), req.getType());
    }

    // ==================== 密码重置核心逻辑 ====================
    @Override
    public void resetPassword(ResetPasswordRequest req) {
        // 步骤1: 验证短信验证码
        boolean ok = smsCodeService.verifyCode(req.getPhone(), "resetPwd", req.getVerifyCode());
        if (!ok) {
            throw BizException.badRequest("验证码错误");
        }

        // 步骤2: 根据手机号查找用户
        UserAccount user = userAccountMapper.selectOne(new LambdaQueryWrapper<UserAccount>()
                .eq(UserAccount::getPhone, req.getPhone())
                .last("limit 1"));
        if (user == null) {
            throw BizException.badRequest("用户不存在");
        }
        
        // 步骤3: 更新用户密码
        user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        userAccountMapper.updateById(user);
    }

    // ==================== JWT令牌刷新逻辑 ====================
    @Override
    public LoginResponse refreshToken(long userId) {
        // 步骤1: 根据用户ID查找用户
        UserAccount user = userAccountMapper.selectById(userId);
        if (user == null) {
            throw BizException.unauthorized("未登录/token过期");
        }
        
        // 步骤2: 生成新的JWT令牌
        String token = jwtTokenUtil.generateToken(user.getUserId(), user.getUsername());
        return new LoginResponse(token, jwtProperties.getExpireSeconds(),
                new LoginResponse.UserInfo(user.getUserId(), user.getUsername(), user.getPhone(), user.getAvatar(),
                        user.getNickname(), user.getLevel(), user.getPoints()));
    }

    // ==================== 密码匹配工具方法 ====================
    // 支持多种密码格式：BCrypt加密密码和明文密码（向后兼容）
    private boolean passwordMatches(String raw, String stored) {
        if (stored == null) {
            return false;
        }
        // 检查是否为BCrypt加密格式
        if (stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$")) {
            return passwordEncoder.matches(raw, stored);
        }
        // 向后兼容：明文密码比较（不推荐，仅用于历史数据）
        return raw.equals(stored);
    }
}
