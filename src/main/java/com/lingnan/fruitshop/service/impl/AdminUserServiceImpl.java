package com.lingnan.fruitshop.service.impl;

import com.lingnan.fruitshop.service.AdminUserService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingnan.fruitshop.common.exception.BizException;
import com.lingnan.fruitshop.dto.admin.user.AdminUserAdjustBalanceRequest;
import com.lingnan.fruitshop.dto.admin.user.AdminUserAdjustPointsRequest;
import com.lingnan.fruitshop.dto.admin.user.AdminUserUpdateStatusRequest;
import com.lingnan.fruitshop.dto.admin.user.vo.AdminUserDetailResponse;
import com.lingnan.fruitshop.dto.admin.user.vo.AdminUserListItemResponse;
import com.lingnan.fruitshop.dto.admin.user.vo.AdminUserOrderItemResponse;
import com.lingnan.fruitshop.dto.customer.common.vo.PageResponse;
import com.lingnan.fruitshop.entity.*;
import com.lingnan.fruitshop.mapper.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class AdminUserServiceImpl implements AdminUserService {

    private static final DateTimeFormatter DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final UserAccountMapper userAccountMapper;
    private final OrderMapper orderMapper;
    private final UserCouponMapper userCouponMapper;
    private final ShopFavoriteMapper shopFavoriteMapper;
    private final UserAddressMapper userAddressMapper;
    private final UserPointsRecordMapper userPointsRecordMapper;
    private final ShopMapper shopMapper;

    public AdminUserServiceImpl(UserAccountMapper userAccountMapper,
                            OrderMapper orderMapper,
                            UserCouponMapper userCouponMapper,
                            ShopFavoriteMapper shopFavoriteMapper,
                            UserAddressMapper userAddressMapper,
                            UserPointsRecordMapper userPointsRecordMapper,
                            ShopMapper shopMapper) {
        this.userAccountMapper = userAccountMapper;
        this.orderMapper = orderMapper;
        this.userCouponMapper = userCouponMapper;
        this.shopFavoriteMapper = shopFavoriteMapper;
        this.userAddressMapper = userAddressMapper;
        this.userPointsRecordMapper = userPointsRecordMapper;
        this.shopMapper = shopMapper;
    }

    @Override
    public PageResponse<AdminUserListItemResponse> list(int page, int pageSize, String keyword, Integer level, Integer status, String startTime, String endTime) {
        LambdaQueryWrapper<UserAccount> qw = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            qw.and(w -> w.like(UserAccount::getUsername, keyword)
                    .or().like(UserAccount::getPhone, keyword));
        }
        if (level != null) {
            qw.eq(UserAccount::getLevel, level);
        }
        if (status != null) {
            qw.eq(UserAccount::getStatus, status);
        }
        if (startTime != null && !startTime.isBlank()) {
            qw.ge(UserAccount::getCreateTime, startTime);
        }
        if (endTime != null && !endTime.isBlank()) {
            qw.le(UserAccount::getCreateTime, endTime);
        }
        qw.orderByDesc(UserAccount::getCreateTime);

        Page<UserAccount> p = new Page<>(page, pageSize);
        Page<UserAccount> result = userAccountMapper.selectPage(p, qw);
        if (result.getRecords().isEmpty()) {
            return new PageResponse<>(0, List.of());
        }

        Set<Long> userIds = new HashSet<>();
        for (UserAccount u : result.getRecords()) {
            userIds.add(u.getUserId());
        }

        Map<Long, Long> orderCountMap = new HashMap<>();
        Map<Long, BigDecimal> totalAmountMap = new HashMap<>();
        List<Order> orders = orderMapper.selectList(new LambdaQueryWrapper<Order>().in(Order::getUserId, userIds));
        for (Order o : orders) {
            orderCountMap.put(o.getUserId(), orderCountMap.getOrDefault(o.getUserId(), 0L) + 1);
            BigDecimal amt = o.getActualAmount() == null ? BigDecimal.ZERO : o.getActualAmount();
            totalAmountMap.put(o.getUserId(), totalAmountMap.getOrDefault(o.getUserId(), BigDecimal.ZERO).add(amt));
        }

        List<AdminUserListItemResponse> list = result.getRecords().stream().map(u -> new AdminUserListItemResponse(
                u.getUserId(),
                u.getUsername(),
                u.getNickname(),
                u.getAvatar(),
                u.getPhone(),
                u.getEmail(),
                u.getGender(),
                u.getLevel(),
                levelName(u.getLevel()),
                u.getPoints(),
                u.getBalance(),
                u.getStatus(),
                orderCountMap.getOrDefault(u.getUserId(), 0L),
                totalAmountMap.getOrDefault(u.getUserId(), BigDecimal.ZERO),
                format(u.getLastLoginTime()),
                format(u.getCreateTime())
        )).toList();

        return new PageResponse<>(result.getTotal(), list);
    }

    @Override
    public AdminUserDetailResponse detail(long userId) {
        UserAccount u = userAccountMapper.selectById(userId);
        if (u == null) {
            throw BizException.notFound("用户不存在");
        }

        long orderCount = safeLong(orderMapper.selectCount(new LambdaQueryWrapper<Order>().eq(Order::getUserId, userId)));
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (Order o : orderMapper.selectList(new LambdaQueryWrapper<Order>().eq(Order::getUserId, userId))) {
            if (o.getActualAmount() != null) {
                totalAmount = totalAmount.add(o.getActualAmount());
            }
        }

        long couponCount = safeLong(userCouponMapper.selectCount(new LambdaQueryWrapper<UserCoupon>().eq(UserCoupon::getUserId, userId)));
        long favoriteCount = safeLong(shopFavoriteMapper.selectCount(new LambdaQueryWrapper<ShopFavorite>().eq(ShopFavorite::getUserId, userId)));
        long addressCount = safeLong(userAddressMapper.selectCount(new LambdaQueryWrapper<UserAddress>().eq(UserAddress::getUserId, userId)));

        String birthday = u.getBirthday() == null ? null : u.getBirthday().toString();

        return new AdminUserDetailResponse(
                u.getUserId(),
                u.getUsername(),
                u.getNickname(),
                u.getAvatar(),
                u.getPhone(),
                u.getEmail(),
                u.getGender(),
                birthday,
                u.getLevel(),
                levelName(u.getLevel()),
                u.getPoints(),
                u.getBalance(),
                u.getFrozenBalance(),
                u.getStatus(),
                orderCount,
                totalAmount,
                couponCount,
                favoriteCount,
                addressCount,
                format(u.getLastLoginTime()),
                u.getLastLoginIp(),
                format(u.getCreateTime())
        );
    }

    @Override
    public void updateStatus(AdminUserUpdateStatusRequest req) {
        UserAccount u = userAccountMapper.selectById(req.getUserId());
        if (u == null) {
            throw BizException.notFound("用户不存在");
        }
        u.setStatus(req.getStatus());
        userAccountMapper.updateById(u);
    }

    @Override
    public void adjustBalance(AdminUserAdjustBalanceRequest req) {
        UserAccount u = userAccountMapper.selectById(req.getUserId());
        if (u == null) {
            throw BizException.notFound("用户不存在");
        }
        BigDecimal amount = req.getAmount() == null ? BigDecimal.ZERO : req.getAmount();
        BigDecimal current = u.getBalance() == null ? BigDecimal.ZERO : u.getBalance();

        BigDecimal updated;
        if ("add".equalsIgnoreCase(req.getType())) {
            updated = current.add(amount);
        } else if ("deduct".equalsIgnoreCase(req.getType())) {
            updated = current.subtract(amount);
        } else {
            throw BizException.badRequest("type 参数错误");
        }
        if (updated.compareTo(BigDecimal.ZERO) < 0) {
            updated = BigDecimal.ZERO;
        }

        userAccountMapper.update(null, new LambdaUpdateWrapper<UserAccount>()
                .eq(UserAccount::getUserId, u.getUserId())
                .set(UserAccount::getBalance, updated));
    }

    @Override
    public void adjustPoints(AdminUserAdjustPointsRequest req) {
        UserAccount u = userAccountMapper.selectById(req.getUserId());
        if (u == null) {
            throw BizException.notFound("用户不存在");
        }
        int points = req.getPoints() == null ? 0 : req.getPoints();
        int current = u.getPoints() == null ? 0 : u.getPoints();

        int updated;
        int recordType;
        if ("add".equalsIgnoreCase(req.getType())) {
            updated = current + points;
            recordType = 1;
        } else if ("deduct".equalsIgnoreCase(req.getType())) {
            updated = current - points;
            recordType = 2;
        } else {
            throw BizException.badRequest("type 参数错误");
        }
        if (updated < 0) {
            updated = 0;
        }

        userAccountMapper.update(null, new LambdaUpdateWrapper<UserAccount>()
                .eq(UserAccount::getUserId, u.getUserId())
                .set(UserAccount::getPoints, updated));

        UserPointsRecord record = new UserPointsRecord();
        record.setUserId(u.getUserId());
        record.setType(recordType);
        record.setPoints(points);
        record.setDescription(req.getReason());
        userPointsRecordMapper.insert(record);
    }

    @Override
    public PageResponse<AdminUserOrderItemResponse> userOrders(long userId, int page, int pageSize) {
        UserAccount u = userAccountMapper.selectById(userId);
        if (u == null) {
            throw BizException.notFound("用户不存在");
        }

        LambdaQueryWrapper<Order> qw = new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .orderByDesc(Order::getCreateTime);

        Page<Order> p = new Page<>(page, pageSize);
        Page<Order> result = orderMapper.selectPage(p, qw);
        if (result.getRecords().isEmpty()) {
            return new PageResponse<>(0, List.of());
        }

        Set<Long> shopIds = new HashSet<>();
        for (Order o : result.getRecords()) {
            if (o.getShopId() != null) {
                shopIds.add(o.getShopId());
            }
        }
        Map<Long, Shop> shopMap = new HashMap<>();
        if (!shopIds.isEmpty()) {
            for (Shop s : shopMapper.selectBatchIds(shopIds)) {
                shopMap.put(s.getShopId(), s);
            }
        }

        List<AdminUserOrderItemResponse> list = result.getRecords().stream().map(o -> {
            Shop s = o.getShopId() == null ? null : shopMap.get(o.getShopId());
            return new AdminUserOrderItemResponse(
                    o.getOrderId(),
                    o.getOrderNo(),
                    o.getShopId(),
                    s == null ? null : s.getShopName(),
                    o.getStatus(),
                    orderStatusText(o.getStatus()),
                    o.getActualAmount(),
                    format(o.getCreateTime())
            );
        }).toList();

        return new PageResponse<>(result.getTotal(), list);
    }

    private String levelName(Integer level) {
        if (level == null) {
            return "普通会员";
        }
        return switch (level) {
            case 1 -> "普通会员";
            case 2 -> "银牌会员";
            case 3 -> "金牌会员";
            case 4 -> "钻石会员";
            default -> "普通会员";
        };
    }

    private String orderStatusText(Integer status) {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case 1 -> "待付款";
            case 2 -> "待发货";
            case 3 -> "待收货";
            case 4 -> "待评价";
            case 5 -> "已完成";
            case 6 -> "已取消";
            default -> "未知";
        };
    }

    private long safeLong(Long v) {
        return v == null ? 0 : v;
    }

    private String format(LocalDateTime dt) {
        return dt == null ? null : DATETIME.format(dt);
    }
}
