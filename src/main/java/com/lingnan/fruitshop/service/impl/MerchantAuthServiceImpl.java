package com.lingnan.fruitshop.service.impl;

import com.lingnan.fruitshop.service.MerchantAuthService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lingnan.fruitshop.common.exception.BizException;
import com.lingnan.fruitshop.dto.merchant.auth.*;
import com.lingnan.fruitshop.dto.merchant.auth.vo.MerchantLoginResponse;
import com.lingnan.fruitshop.dto.merchant.auth.vo.MerchantRegisterResponse;
import com.lingnan.fruitshop.entity.MerchantAccount;
import com.lingnan.fruitshop.entity.MerchantApplication;
import com.lingnan.fruitshop.entity.Shop;
import com.lingnan.fruitshop.mapper.MerchantAccountMapper;
import com.lingnan.fruitshop.mapper.MerchantApplicationMapper;
import com.lingnan.fruitshop.mapper.ShopMapper;
import com.lingnan.fruitshop.security.JwtProperties;
import com.lingnan.fruitshop.security.JwtTokenUtil;
import com.lingnan.fruitshop.service.SmsCodeService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class MerchantAuthServiceImpl implements MerchantAuthService {

    private final MerchantApplicationMapper merchantApplicationMapper;
    private final MerchantAccountMapper merchantAccountMapper;
    private final ShopMapper shopMapper;
    private final SmsCodeService smsCodeService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;
    private final JwtProperties jwtProperties;

    public MerchantAuthServiceImpl(MerchantApplicationMapper merchantApplicationMapper,
                               MerchantAccountMapper merchantAccountMapper,
                               ShopMapper shopMapper,
                               SmsCodeService smsCodeService,
                               PasswordEncoder passwordEncoder,
                               JwtTokenUtil jwtTokenUtil,
                               JwtProperties jwtProperties) {
        this.merchantApplicationMapper = merchantApplicationMapper;
        this.merchantAccountMapper = merchantAccountMapper;
        this.shopMapper = shopMapper;
        this.smsCodeService = smsCodeService;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenUtil = jwtTokenUtil;
        this.jwtProperties = jwtProperties;
    }

    @Override
    public MerchantRegisterResponse register(MerchantRegisterRequest req) {
        boolean ok = smsCodeService.verifyCode(req.getContactPhone(), "register", req.getVerifyCode());
        if (!ok) {
            throw BizException.badRequest("验证码错误");
        }

        Long exists = merchantAccountMapper.selectCount(new LambdaQueryWrapper<MerchantAccount>()
                .eq(MerchantAccount::getContactPhone, req.getContactPhone()));
        if (exists != null && exists > 0) {
            throw BizException.badRequest("手机号已存在");
        }

        MerchantApplication app = new MerchantApplication();
        app.setShopName(req.getShopName());
        app.setContactName(req.getContactName());
        app.setContactPhone(req.getContactPhone());
        app.setEmail(req.getEmail());
        app.setBusinessLicense(req.getBusinessLicense());
        app.setIdCardFront(req.getIdCardFront());
        app.setIdCardBack(req.getIdCardBack());
        app.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        app.setStatus(0);
        merchantApplicationMapper.insert(app);

        MerchantAccount account = new MerchantAccount();
        account.setApplicationId(app.getApplicationId());
        account.setShopId(null);
        account.setShopName(req.getShopName());
        account.setContactName(req.getContactName());
        account.setContactPhone(req.getContactPhone());
        account.setEmail(req.getEmail());
        account.setBusinessLicense(req.getBusinessLicense());
        account.setIdCardFront(req.getIdCardFront());
        account.setIdCardBack(req.getIdCardBack());
        account.setPasswordHash(app.getPasswordHash());
        account.setStatus(0);
        merchantAccountMapper.insert(account);

        return new MerchantRegisterResponse(app.getApplicationId(), "pending");
    }

    @Override
    public MerchantLoginResponse login(MerchantLoginRequest req) {
        MerchantAccount merchant = merchantAccountMapper.selectOne(new LambdaQueryWrapper<MerchantAccount>()
                .eq(MerchantAccount::getContactPhone, req.getAccount())
                .or()
                .eq(MerchantAccount::getShopName, req.getAccount())
                .last("limit 1"));

        if (merchant == null) {
            throw BizException.badRequest("账号或密码错误");
        }

        if (merchant.getStatus() == null || merchant.getStatus() != 1) {
            throw BizException.forbidden("账号未审核或已禁用");
        }

        if (!passwordMatches(req.getPassword(), merchant.getPasswordHash())) {
            throw BizException.badRequest("账号或密码错误");
        }

        Integer shopStatus = null;
        if (merchant.getShopId() != null) {
            Shop shop = shopMapper.selectById(merchant.getShopId());
            shopStatus = shop == null ? null : shop.getStatus();
        }

        String token = jwtTokenUtil.generateMerchantToken(merchant.getMerchantId(), merchant.getShopId(), req.getAccount());

        return new MerchantLoginResponse(token, jwtProperties.getExpireSeconds(),
                new MerchantLoginResponse.MerchantInfo(
                        merchant.getMerchantId(),
                        merchant.getShopId(),
                        merchant.getShopName(),
                        merchant.getContactName(),
                        merchant.getContactPhone(),
                        merchant.getStatus(),
                        shopStatus
                ));
    }

    @Override
    public MerchantLoginResponse getMerchantInfo(long merchantId) {
        MerchantAccount merchant = merchantAccountMapper.selectById(merchantId);
        
        if (merchant == null) {
            throw BizException.notFound("商家不存在");
        }

        if (merchant.getStatus() == null || merchant.getStatus() != 1) {
            throw BizException.forbidden("账号未审核或已禁用");
        }

        Integer shopStatus = null;
        if (merchant.getShopId() != null) {
            Shop shop = shopMapper.selectById(merchant.getShopId());
            shopStatus = shop == null ? null : shop.getStatus();
        }

        // 不生成新的token，返回空字符串
        return new MerchantLoginResponse("", jwtProperties.getExpireSeconds(),
                new MerchantLoginResponse.MerchantInfo(
                        merchant.getMerchantId(),
                        merchant.getShopId(),
                        merchant.getShopName(),
                        merchant.getContactName(),
                        merchant.getContactPhone(),
                        merchant.getStatus(),
                        shopStatus
                ));
    }

    @Override
    public void changePassword(long merchantId, MerchantChangePasswordRequest req) {
        MerchantAccount merchant = merchantAccountMapper.selectById(merchantId);
        if (merchant == null) {
            throw BizException.notFound("商家不存在");
        }
        if (!passwordMatches(req.getOldPassword(), merchant.getPasswordHash())) {
            throw BizException.badRequest("旧密码错误");
        }
        merchant.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        merchantAccountMapper.updateById(merchant);
    }

    @Override
    public void resetPassword(MerchantResetPasswordRequest req) {
        boolean ok = smsCodeService.verifyCode(req.getPhone(), "resetPwd", req.getVerifyCode());
        if (!ok) {
            throw BizException.badRequest("验证码错误");
        }
        MerchantAccount merchant = merchantAccountMapper.selectOne(new LambdaQueryWrapper<MerchantAccount>()
                .eq(MerchantAccount::getContactPhone, req.getPhone())
                .last("limit 1"));
        if (merchant == null) {
            throw BizException.notFound("商家不存在");
        }
        merchant.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        merchantAccountMapper.updateById(merchant);
    }

    @Override
    public MerchantLoginResponse refreshToken(long merchantId) {
        MerchantAccount merchant = merchantAccountMapper.selectById(merchantId);
        if (merchant == null) {
            throw BizException.unauthorized("未登录/token过期");
        }
        
        Integer shopStatus = null;
        if (merchant.getShopId() != null) {
            Shop shop = shopMapper.selectById(merchant.getShopId());
            shopStatus = shop == null ? null : shop.getStatus();
        }

        String token = jwtTokenUtil.generateMerchantToken(merchant.getMerchantId(), merchant.getShopId(), merchant.getContactPhone());

        return new MerchantLoginResponse(token, jwtProperties.getExpireSeconds(),
                new MerchantLoginResponse.MerchantInfo(
                        merchant.getMerchantId(),
                        merchant.getShopId(),
                        merchant.getShopName(),
                        merchant.getContactName(),
                        merchant.getContactPhone(),
                        merchant.getStatus(),
                        shopStatus
                ));
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
