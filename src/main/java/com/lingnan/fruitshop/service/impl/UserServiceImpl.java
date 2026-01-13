package com.lingnan.fruitshop.service.impl;

import com.lingnan.fruitshop.service.SmsCodeService;
import com.lingnan.fruitshop.service.UserService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingnan.fruitshop.common.exception.BizException;
import com.lingnan.fruitshop.dto.customer.user.*;
import com.lingnan.fruitshop.dto.customer.user.vo.BalanceResponse;
import com.lingnan.fruitshop.dto.customer.user.vo.PointsRecordsResponse;
import com.lingnan.fruitshop.dto.customer.user.vo.UserProfileResponse;
import com.lingnan.fruitshop.entity.UserAccount;
import com.lingnan.fruitshop.entity.UserPointsRecord;
import com.lingnan.fruitshop.mapper.UserAccountMapper;
import com.lingnan.fruitshop.mapper.UserCouponMapper;
import com.lingnan.fruitshop.mapper.UserPointsRecordMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    // ==================== 常量定义 ====================
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ==================== 依赖注入区域 ====================
    // 注入用户相关的数据访问层：用户账户、积分记录、优惠券、密码编码器、短信服务
    private final UserAccountMapper userAccountMapper;
    private final UserPointsRecordMapper userPointsRecordMapper;
    private final UserCouponMapper userCouponMapper;
    private final PasswordEncoder passwordEncoder;
    private final SmsCodeService smsCodeService;

    public UserServiceImpl(UserAccountMapper userAccountMapper,
                       UserPointsRecordMapper userPointsRecordMapper,
                       UserCouponMapper userCouponMapper,
                       PasswordEncoder passwordEncoder,
                       SmsCodeService smsCodeService) {
        this.userAccountMapper = userAccountMapper;
        this.userPointsRecordMapper = userPointsRecordMapper;
        this.userCouponMapper = userCouponMapper;
        this.passwordEncoder = passwordEncoder;
        this.smsCodeService = smsCodeService;
    }

    // ==================== 用户资料查询核心逻辑 ====================
    // 查询用户完整信息，包括基本资料、等级、积分、余额、优惠券数量等
    @Override
    public UserProfileResponse profile(long userId) {
        // 步骤1: 查询用户基本信息
        UserAccount user = userAccountMapper.selectById(userId);
        if (user == null) {
            throw BizException.notFound("用户不存在");
        }

        // 步骤2: 统计用户可用优惠券数量
        Long couponCountLong = userCouponMapper.selectCount(new LambdaQueryWrapper<com.lingnan.fruitshop.entity.UserCoupon>()
                .eq(com.lingnan.fruitshop.entity.UserCoupon::getUserId, userId)
                .eq(com.lingnan.fruitshop.entity.UserCoupon::getStatus, 0));  // 状态0表示可用
        int couponCount = couponCountLong == null ? 0 : couponCountLong.intValue();

        // 步骤3: 构建用户资料响应对象
        return new UserProfileResponse(
                user.getUserId(),
                user.getUsername(),
                user.getNickname(),
                user.getAvatar(),
                user.getPhone(),
                user.getEmail(),
                user.getGender(),
                user.getBirthday() == null ? null : user.getBirthday().format(DATE),    // 格式化生日
                user.getLevel(),
                levelName(user.getLevel()),                                              // 获取等级名称
                user.getPoints() == null ? 0 : user.getPoints(),                        // 积分处理
                user.getBalance() == null ? BigDecimal.ZERO : user.getBalance(),        // 余额处理
                couponCount,                                                             // 优惠券数量
                user.getCreateTime() == null ? null : user.getCreateTime().format(DATETIME) // 格式化注册时间
        );
    }

/**
 * 更新用户资料信息的方法
 * @param userId 用户ID
 * @param req 包含更新资料的请求对象
 * @throws BizException 当用户不存在时抛出异常
 */
    @Override
    public void updateProfile(long userId, UpdateProfileRequest req) {
    // 根据用户ID查询用户信息
        UserAccount user = userAccountMapper.selectById(userId);
    // 检查用户是否存在，不存在则抛出异常
        if (user == null) {
            throw BizException.notFound("用户不存在");
        }

    // 如果请求中包含昵称，则更新用户昵称
        if (req.getNickname() != null) {
            user.setNickname(req.getNickname());
        }
    // 如果请求中包含头像，则更新用户头像
        if (req.getAvatar() != null) {
            user.setAvatar(req.getAvatar());
        }
    // 如果请求中包含性别，则更新用户性别
        if (req.getGender() != null) {
            user.setGender(req.getGender());
        }
    // 如果请求中包含邮箱，则更新用户邮箱
        if (req.getEmail() != null) {
            user.setEmail(req.getEmail());
        }
    // 如果请求中包含生日且不为空字符串，则解析并更新用户生日
        if (req.getBirthday() != null && !req.getBirthday().isBlank()) {
            user.setBirthday(LocalDate.parse(req.getBirthday(), DATE));
        }

    // 更新用户信息到数据库
        userAccountMapper.updateById(user);
    }

    @Override
    /**
     * 修改用户密码的方法
     * @param userId 用户ID，用于标识需要修改密码的用户
     * @param req 包含旧密码和新密码的请求对象
     * @throws BizException 当用户不存在或旧密码错误时抛出业务异常
     */
    public void changePassword(long userId, ChangePasswordRequest req) {
        // 根据用户ID查询用户账户信息
        UserAccount user = userAccountMapper.selectById(userId);
        // 如果用户不存在，抛出"用户不存在"异常
        if (user == null) {
            throw BizException.notFound("用户不存在");
        }

        // 验证旧密码是否正确
        if (!passwordMatches(req.getOldPassword(), user.getPasswordHash())) {
            // 如果旧密码不匹配，抛出"旧密码错误"异常
            throw BizException.badRequest("旧密码错误");
        }

        // 使用密码编码器对新密码进行加密，并更新用户密码
        user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        // 更新用户账户信息到数据库
        userAccountMapper.updateById(user);
    }

    @Override
    public void changePhone(long userId, ChangePhoneRequest req) {
        boolean ok = smsCodeService.verifyCode(req.getNewPhone(), "changePhone", req.getVerifyCode());
        if (!ok) {
            throw BizException.badRequest("验证码错误");
        }

        Long exists = userAccountMapper.selectCount(new LambdaQueryWrapper<UserAccount>()
                .eq(UserAccount::getPhone, req.getNewPhone()));
        if (exists != null && exists > 0) {
            throw BizException.badRequest("手机号已存在");
        }

        UserAccount user = userAccountMapper.selectById(userId);
        if (user == null) {
            throw BizException.notFound("用户不存在");
        }
        user.setPhone(req.getNewPhone());
        userAccountMapper.updateById(user);
    }

    @Override
    public BalanceResponse balance(long userId) {
        UserAccount user = userAccountMapper.selectById(userId);
        if (user == null) {
            throw BizException.notFound("用户不存在");
        }
        return new BalanceResponse(
                user.getBalance() == null ? BigDecimal.ZERO : user.getBalance(),
                user.getFrozenBalance() == null ? BigDecimal.ZERO : user.getFrozenBalance());
    }

    @Override
    public PointsRecordsResponse pointsRecords(long userId, int page, int pageSize) {
        UserAccount user = userAccountMapper.selectById(userId);
        if (user == null) {
            throw BizException.notFound("用户不存在");
        }

        Page<UserPointsRecord> p = new Page<>(page, pageSize);
        Page<UserPointsRecord> result = userPointsRecordMapper.selectPage(p,
                new LambdaQueryWrapper<UserPointsRecord>()
                        .eq(UserPointsRecord::getUserId, userId)
                        .orderByDesc(UserPointsRecord::getCreateTime));

        List<PointsRecordsResponse.Item> list = result.getRecords().stream().map(r -> new PointsRecordsResponse.Item(
                r.getRecordId(),
                r.getType(),
                pointsTypeName(r.getType()),
                r.getPoints(),
                r.getDescription(),
                r.getCreateTime() == null ? null : r.getCreateTime().format(DATETIME)
        )).toList();

        return new PointsRecordsResponse(result.getTotal(), user.getPoints() == null ? 0 : user.getPoints(), list);
    }

    @Override
    @Transactional
    public BalanceResponse recharge(long userId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw BizException.badRequest("充值金额必须大于0");
        }
        UserAccount user = userAccountMapper.selectById(userId);
        if (user == null) {
            throw BizException.notFound("用户不存在");
        }
        BigDecimal current = user.getBalance() == null ? BigDecimal.ZERO : user.getBalance();
        user.setBalance(current.add(amount));
        userAccountMapper.updateById(user);
        return new BalanceResponse(user.getBalance(), user.getFrozenBalance() == null ? BigDecimal.ZERO : user.getFrozenBalance());
    }

    private String levelName(Integer level) {
        if (level == null) {
            return "普通会员";
        }
        return switch (level) {
            case 1 -> "普通会员";
            case 2 -> "银牌会员";
            case 3 -> "金牌会员";
            default -> "会员";
        };
    }

    private String pointsTypeName(Integer type) {
        if (type == null) {
            return "未知";
        }
        return switch (type) {
            case 1 -> "购物获得";
            case 2 -> "兑换使用";
            case 3 -> "系统调整";
            default -> "未知";
        };
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
