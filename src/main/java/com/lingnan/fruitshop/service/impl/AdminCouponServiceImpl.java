package com.lingnan.fruitshop.service.impl;

import com.lingnan.fruitshop.service.AdminCouponService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingnan.fruitshop.common.exception.BizException;
import com.lingnan.fruitshop.dto.admin.coupon.AdminCouponAddRequest;
import com.lingnan.fruitshop.dto.admin.coupon.AdminCouponUpdateRequest;
import com.lingnan.fruitshop.dto.admin.coupon.vo.AdminCouponDetailResponse;
import com.lingnan.fruitshop.dto.admin.coupon.vo.AdminCouponListItemResponse;
import com.lingnan.fruitshop.dto.customer.common.vo.PageResponse;
import com.lingnan.fruitshop.entity.Coupon;
import com.lingnan.fruitshop.mapper.CouponMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class AdminCouponServiceImpl implements AdminCouponService {

    private static final DateTimeFormatter DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final CouponMapper couponMapper;

    public AdminCouponServiceImpl(CouponMapper couponMapper) {
        this.couponMapper = couponMapper;
    }

    @Override
    public PageResponse<AdminCouponListItemResponse> list(int page, int pageSize, Integer type, Integer status) {
        LambdaQueryWrapper<Coupon> qw = new LambdaQueryWrapper<>();
        if (type != null) {
            qw.eq(Coupon::getCouponType, type);
        }
        if (status != null) {
            qw.eq(Coupon::getStatus, status);
        }
        qw.orderByDesc(Coupon::getCreateTime);

        Page<Coupon> p = new Page<>(page, pageSize);
        Page<Coupon> result = couponMapper.selectPage(p, qw);
        if (result.getRecords().isEmpty()) {
            return new PageResponse<>(0, List.of());
        }

        List<AdminCouponListItemResponse> list = result.getRecords().stream().map(c -> new AdminCouponListItemResponse(
                c.getCouponId(),
                c.getCouponName(),
                c.getCouponType(),
                c.getDiscountType(),
                c.getDiscountValue(),
                c.getMinAmount(),
                c.getTotalCount(),
                c.getReceivedCount(),
                c.getUsedCount(),
                format(c.getStartTime()),
                format(c.getEndTime()),
                c.getStatus(),
                format(c.getCreateTime())
        )).toList();

        return new PageResponse<>(result.getTotal(), list);
    }

    @Override
    public AdminCouponDetailResponse detail(long couponId) {
        Coupon c = couponMapper.selectById(couponId);
        if (c == null) {
            throw BizException.notFound("优惠券不存在");
        }
        return new AdminCouponDetailResponse(
                c.getCouponId(),
                c.getCouponName(),
                c.getCouponType(),
                c.getDiscountType(),
                c.getDiscountValue(),
                c.getMinAmount(),
                c.getTotalCount(),
                c.getLimitPerUser(),
                c.getValidDays(),
                format(c.getStartTime()),
                format(c.getEndTime()),
                c.getStatus(),
                c.getDescription()
        );
    }

    @Override
    public void add(AdminCouponAddRequest req) {
        Coupon c = new Coupon();
        c.setCouponName(req.getCouponName());
        c.setCouponType(req.getCouponType());
        c.setDiscountType(req.getDiscountType());
        c.setDiscountValue(req.getDiscountValue());
        c.setMinAmount(req.getMinAmount());
        c.setTotalCount(req.getTotalCount());
        c.setReceivedCount(0);
        c.setUsedCount(0);
        c.setLimitPerUser(req.getLimitPerUser());
        c.setValidDays(req.getValidDays());
        c.setStartTime(parse(req.getStartTime()));
        c.setEndTime(parse(req.getEndTime()));
        c.setStatus(1);
        c.setDescription(req.getDescription());
        couponMapper.insert(c);
    }

    @Override
    public void update(long couponId, AdminCouponUpdateRequest req) {
        Coupon c = couponMapper.selectById(couponId);
        if (c == null) {
            throw BizException.notFound("优惠券不存在");
        }

        couponMapper.update(null, new LambdaUpdateWrapper<Coupon>()
                .eq(Coupon::getCouponId, couponId)
                .set(Coupon::getCouponName, req.getCouponName())
                .set(Coupon::getCouponType, req.getCouponType())
                .set(Coupon::getDiscountType, req.getDiscountType())
                .set(Coupon::getDiscountValue, req.getDiscountValue())
                .set(Coupon::getMinAmount, req.getMinAmount())
                .set(Coupon::getTotalCount, req.getTotalCount())
                .set(Coupon::getLimitPerUser, req.getLimitPerUser())
                .set(Coupon::getValidDays, req.getValidDays())
                .set(Coupon::getStartTime, parse(req.getStartTime()))
                .set(Coupon::getEndTime, parse(req.getEndTime()))
                .set(req.getStatus() == null, Coupon::getStatus, c.getStatus())
                .set(req.getStatus() != null, Coupon::getStatus, req.getStatus())
                .set(Coupon::getDescription, req.getDescription()));
    }

    @Override
    public void delete(long couponId) {
        couponMapper.deleteById(couponId);
    }

    private String format(LocalDateTime dt) {
        return dt == null ? null : DATETIME.format(dt);
    }

    private LocalDateTime parse(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(s, DATETIME);
        } catch (Exception e) {
            return null;
        }
    }
}
