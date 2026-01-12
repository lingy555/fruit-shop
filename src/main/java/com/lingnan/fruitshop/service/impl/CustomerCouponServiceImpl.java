package com.lingnan.fruitshop.service.impl;

import com.lingnan.fruitshop.service.CustomerCouponService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.lingnan.fruitshop.common.exception.BizException;
import com.lingnan.fruitshop.dto.customer.common.vo.PageResponse;
import com.lingnan.fruitshop.dto.customer.coupon.vo.CustomerCouponAvailableItemResponse;
import com.lingnan.fruitshop.dto.customer.coupon.vo.CustomerMyCouponItemResponse;
import com.lingnan.fruitshop.entity.Coupon;
import com.lingnan.fruitshop.entity.UserCoupon;
import com.lingnan.fruitshop.mapper.CouponMapper;
import com.lingnan.fruitshop.mapper.UserCouponMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class CustomerCouponServiceImpl implements CustomerCouponService {

    private static final DateTimeFormatter DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final CouponMapper couponMapper;
    private final UserCouponMapper userCouponMapper;

    public CustomerCouponServiceImpl(CouponMapper couponMapper, UserCouponMapper userCouponMapper) {
        this.couponMapper = couponMapper;
        this.userCouponMapper = userCouponMapper;
    }

    @Override
    public PageResponse<CustomerCouponAvailableItemResponse> available(long userId, int page, int pageSize) {
        LocalDateTime now = LocalDateTime.now();

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Coupon> p = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, pageSize);
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Coupon> result = couponMapper.selectPage(p,
                new LambdaQueryWrapper<Coupon>()
                        .eq(Coupon::getStatus, 1)
                        .and(w -> w.isNull(Coupon::getStartTime).or().le(Coupon::getStartTime, now))
                        .and(w -> w.isNull(Coupon::getEndTime).or().ge(Coupon::getEndTime, now))
                        .orderByDesc(Coupon::getCreateTime));

        if (result.getRecords().isEmpty()) {
            return new PageResponse<>(0, List.of());
        }

        Set<Long> couponIds = new HashSet<>();
        for (Coupon c : result.getRecords()) {
            if (c.getCouponId() != null) {
                couponIds.add(c.getCouponId());
            }
        }

        Map<Long, Integer> receivedByMeMap = new HashMap<>();
        if (!couponIds.isEmpty()) {
            List<UserCoupon> ucs = userCouponMapper.selectList(new LambdaQueryWrapper<UserCoupon>()
                    .eq(UserCoupon::getUserId, userId)
                    .in(UserCoupon::getCouponId, couponIds));
            for (UserCoupon uc : ucs) {
                if (uc.getCouponId() == null) {
                    continue;
                }
                receivedByMeMap.merge(uc.getCouponId(), 1, Integer::sum);
            }
        }

        List<CustomerCouponAvailableItemResponse> list = result.getRecords().stream().map(c -> {
            int total = c.getTotalCount() == null ? 0 : c.getTotalCount();
            int received = c.getReceivedCount() == null ? 0 : c.getReceivedCount();
            int remaining = Math.max(0, total - received);

            int receivedByMe = receivedByMeMap.getOrDefault(c.getCouponId(), 0);
            int limitPerUser = c.getLimitPerUser() == null ? Integer.MAX_VALUE : c.getLimitPerUser();

            boolean canReceive = remaining > 0 && receivedByMe < limitPerUser;

            return new CustomerCouponAvailableItemResponse(
                    c.getCouponId(),
                    c.getCouponName(),
                    c.getCouponType(),
                    c.getDiscountType(),
                    c.getDiscountValue(),
                    c.getMinAmount(),
                    c.getTotalCount(),
                    c.getReceivedCount(),
                    remaining,
                    c.getLimitPerUser(),
                    receivedByMe,
                    canReceive,
                    format(c.getStartTime()),
                    format(c.getEndTime()),
                    c.getDescription()
            );
        }).toList();

        return new PageResponse<>(result.getTotal(), list);
    }

    @Transactional
    @Override
    public void receive(long userId, long couponId) {
        Coupon c = couponMapper.selectById(couponId);
        if (c == null) {
            throw BizException.notFound("优惠券不存在");
        }
        if (c.getStatus() == null || c.getStatus() != 1) {
            throw BizException.badRequest("优惠券不可领取");
        }

        LocalDateTime now = LocalDateTime.now();
        if (c.getStartTime() != null && now.isBefore(c.getStartTime())) {
            throw BizException.badRequest("优惠券未开始");
        }
        if (c.getEndTime() != null && now.isAfter(c.getEndTime())) {
            throw BizException.badRequest("优惠券已结束");
        }

        int limitPerUser = c.getLimitPerUser() == null ? Integer.MAX_VALUE : c.getLimitPerUser();
        Long receivedByMeLong = userCouponMapper.selectCount(new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getCouponId, couponId));
        int receivedByMe = receivedByMeLong == null ? 0 : receivedByMeLong.intValue();
        if (receivedByMe >= limitPerUser) {
            throw BizException.badRequest("已达到领取上限");
        }

        int updated = couponMapper.update(null, new UpdateWrapper<Coupon>()
                .eq("coupon_id", couponId)
                .apply("received_count < total_count")
                .setSql("received_count = received_count + 1")
                .set("update_time", now));
        if (updated <= 0) {
            throw BizException.badRequest("优惠券已领完");
        }

        UserCoupon uc = new UserCoupon();
        uc.setUserId(userId);
        uc.setCouponId(couponId);
        uc.setStatus(0);
        uc.setReceiveTime(now);
        userCouponMapper.insert(uc);
    }

    @Override
    public PageResponse<CustomerMyCouponItemResponse> my(long userId, int page, int pageSize, Integer status) {
        List<UserCoupon> ucs = userCouponMapper.selectList(new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getUserId, userId)
                .orderByDesc(UserCoupon::getReceiveTime)
                .last("limit 500"));
        if (ucs.isEmpty()) {
            return new PageResponse<>(0, List.of());
        }

        Set<Long> couponIds = new HashSet<>();
        for (UserCoupon uc : ucs) {
            if (uc.getCouponId() != null) {
                couponIds.add(uc.getCouponId());
            }
        }

        Map<Long, Coupon> couponMap = new HashMap<>();
        if (!couponIds.isEmpty()) {
            for (Coupon c : couponMapper.selectBatchIds(couponIds)) {
                couponMap.put(c.getCouponId(), c);
            }
        }

        LocalDateTime now = LocalDateTime.now();

        List<CustomerMyCouponItemResponse> all = new ArrayList<>();
        for (UserCoupon uc : ucs) {
            Coupon c = couponMap.get(uc.getCouponId());
            if (c == null) {
                continue;
            }

            LocalDateTime expireTime = null;
            if (uc.getReceiveTime() != null && c.getValidDays() != null && c.getValidDays() > 0) {
                expireTime = uc.getReceiveTime().plusDays(c.getValidDays());
            }
            if (c.getEndTime() != null) {
                expireTime = expireTime == null ? c.getEndTime() : (expireTime.isAfter(c.getEndTime()) ? c.getEndTime() : expireTime);
            }

            boolean expired = expireTime != null && now.isAfter(expireTime);

            int computedStatus;
            String statusText;
            if (uc.getStatus() != null && uc.getStatus() == 1) {
                computedStatus = 1;
                statusText = "已使用";
            } else if (expired) {
                computedStatus = 2;
                statusText = "已过期";
            } else {
                computedStatus = 0;
                statusText = "未使用";
            }

            all.add(new CustomerMyCouponItemResponse(
                    uc.getUserCouponId(),
                    c.getCouponId(),
                    c.getCouponName(),
                    c.getDiscountType(),
                    c.getDiscountValue(),
                    c.getMinAmount(),
                    computedStatus,
                    statusText,
                    format(uc.getReceiveTime()),
                    format(uc.getUseTime()),
                    format(expireTime),
                    c.getDescription()
            ));
        }

        List<CustomerMyCouponItemResponse> filtered = all;
        if (status != null) {
            filtered = all.stream().filter(i -> Objects.equals(i.getStatus(), status)).toList();
        }

        int total = filtered.size();
        int from = Math.max(0, (page - 1) * pageSize);
        int to = Math.min(total, from + pageSize);
        List<CustomerMyCouponItemResponse> pageList = from >= to ? List.of() : filtered.subList(from, to);

        return new PageResponse<>(total, pageList);
    }

    private String format(LocalDateTime dt) {
        return dt == null ? null : DATETIME.format(dt);
    }
}
