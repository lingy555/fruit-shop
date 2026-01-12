package com.lingnan.fruitshop.service.impl;

import com.lingnan.fruitshop.service.AdminRefundService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lingnan.fruitshop.common.exception.BizException;
import com.lingnan.fruitshop.dto.admin.refund.AdminRefundArbitrateRequest;
import com.lingnan.fruitshop.dto.admin.refund.vo.AdminRefundDetailResponse;
import com.lingnan.fruitshop.dto.admin.refund.vo.AdminRefundListResponse;
import com.lingnan.fruitshop.entity.*;
import com.lingnan.fruitshop.mapper.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class AdminRefundServiceImpl implements AdminRefundService {

    private static final DateTimeFormatter DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final RefundMapper refundMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final UserAccountMapper userAccountMapper;
    private final ShopMapper shopMapper;
    private final ObjectMapper objectMapper;

    public AdminRefundServiceImpl(RefundMapper refundMapper,
                              OrderMapper orderMapper,
                              OrderItemMapper orderItemMapper,
                              UserAccountMapper userAccountMapper,
                              ShopMapper shopMapper,
                              ObjectMapper objectMapper) {
        this.refundMapper = refundMapper;
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.userAccountMapper = userAccountMapper;
        this.shopMapper = shopMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public AdminRefundListResponse list(int page, int pageSize, String refundNo, Integer status, Long shopId, String keyword) {
        LambdaQueryWrapper<Refund> qw = new LambdaQueryWrapper<>();
        if (refundNo != null && !refundNo.isBlank()) {
            qw.like(Refund::getRefundNo, refundNo);
        }
        if (status != null) {
            qw.eq(Refund::getStatus, status);
        }
        if (shopId != null) {
            qw.eq(Refund::getShopId, shopId);
        }
        if (keyword != null && !keyword.isBlank()) {
            qw.and(w -> w.like(Refund::getReason, keyword)
                    .or().like(Refund::getDescription, keyword));
        }
        qw.orderByDesc(Refund::getCreateTime);

        Page<Refund> p = new Page<>(page, pageSize);
        Page<Refund> result = refundMapper.selectPage(p, qw);
        if (result.getRecords().isEmpty()) {
            return new AdminRefundListResponse(0, List.of());
        }

        Set<Long> orderIds = new HashSet<>();
        Set<Long> userIds = new HashSet<>();
        Set<Long> shopIds = new HashSet<>();
        for (Refund r : result.getRecords()) {
            if (r.getOrderId() != null) orderIds.add(r.getOrderId());
            if (r.getUserId() != null) userIds.add(r.getUserId());
            if (r.getShopId() != null) shopIds.add(r.getShopId());
        }

        Map<Long, Order> orderMap = new HashMap<>();
        if (!orderIds.isEmpty()) {
            for (Order o : orderMapper.selectBatchIds(orderIds)) {
                orderMap.put(o.getOrderId(), o);
            }
        }

        Map<Long, UserAccount> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            for (UserAccount u : userAccountMapper.selectBatchIds(userIds)) {
                userMap.put(u.getUserId(), u);
            }
        }

        Map<Long, Shop> shopMap = new HashMap<>();
        if (!shopIds.isEmpty()) {
            for (Shop s : shopMapper.selectBatchIds(shopIds)) {
                shopMap.put(s.getShopId(), s);
            }
        }

        List<AdminRefundListResponse.Item> list = result.getRecords().stream().map(r -> {
            Order o = r.getOrderId() == null ? null : orderMap.get(r.getOrderId());
            UserAccount u = r.getUserId() == null ? null : userMap.get(r.getUserId());
            Shop s = r.getShopId() == null ? null : shopMap.get(r.getShopId());

            String username = u == null ? "用户" : maskName(u.getNickname() != null ? u.getNickname() : u.getUsername());

            return new AdminRefundListResponse.Item(
                    r.getRefundId(),
                    r.getRefundNo(),
                    r.getOrderId(),
                    o == null ? null : o.getOrderNo(),
                    r.getUserId(),
                    username,
                    r.getShopId(),
                    s == null ? null : s.getShopName(),
                    r.getType(),
                    typeText(r.getType()),
                    r.getReason(),
                    r.getRefundAmount(),
                    r.getStatus(),
                    statusText(r.getStatus()),
                    r.getCreateTime() == null ? null : r.getCreateTime().format(DATETIME)
            );
        }).toList();

        return new AdminRefundListResponse(result.getTotal(), list);
    }

    @Override
    public AdminRefundDetailResponse detail(long refundId) {
        Refund r = refundMapper.selectById(refundId);
        if (r == null) {
            throw BizException.notFound("退款不存在");
        }

        Order o = r.getOrderId() == null ? null : orderMapper.selectById(r.getOrderId());
        UserAccount u = r.getUserId() == null ? null : userAccountMapper.selectById(r.getUserId());
        Shop s = r.getShopId() == null ? null : shopMapper.selectById(r.getShopId());
        OrderItem oi = r.getOrderItemId() == null ? null : orderItemMapper.selectById(r.getOrderItemId());

        String username = u == null ? "用户" : maskName(u.getNickname() != null ? u.getNickname() : u.getUsername());
        String userPhone = u == null ? null : u.getPhone();

        AdminRefundDetailResponse.Product product = null;
        if (oi != null) {
            product = new AdminRefundDetailResponse.Product(oi.getProductId(), oi.getProductName(), oi.getImage(), oi.getSpecs(), oi.getPrice(), oi.getQuantity());
        }

        List<AdminRefundDetailResponse.Timeline> timeline = new ArrayList<>();
        if (r.getCreateTime() != null) {
            timeline.add(new AdminRefundDetailResponse.Timeline(r.getCreateTime().format(DATETIME), "买家申请退款", r.getReason()));
        }
        if (r.getMerchantReply() != null) {
            timeline.add(new AdminRefundDetailResponse.Timeline(r.getUpdateTime() == null ? null : r.getUpdateTime().format(DATETIME), "商家处理", r.getMerchantReply()));
        }
        if (r.getArbitrateResult() != null) {
            timeline.add(new AdminRefundDetailResponse.Timeline(r.getUpdateTime() == null ? null : r.getUpdateTime().format(DATETIME), "平台仲裁", r.getArbitrateReason()));
        }

        return new AdminRefundDetailResponse(
                r.getRefundId(),
                r.getRefundNo(),
                r.getOrderId(),
                o == null ? null : o.getOrderNo(),
                r.getOrderItemId(),
                r.getUserId(),
                username,
                userPhone,
                r.getShopId(),
                s == null ? null : s.getShopName(),
                r.getType(),
                typeText(r.getType()),
                r.getReason(),
                r.getDescription(),
                readJsonArray(r.getImagesJson()),
                r.getRefundAmount(),
                r.getStatus(),
                statusText(r.getStatus()),
                r.getMerchantReply(),
                r.getRejectReason(),
                r.getArbitrateResult(),
                r.getArbitrateReason(),
                product,
                timeline,
                r.getCreateTime() == null ? null : r.getCreateTime().format(DATETIME)
        );
    }

    @Override
    public void arbitrate(AdminRefundArbitrateRequest req) {
        Refund r = refundMapper.selectById(req.getRefundId());
        if (r == null) {
            throw BizException.notFound("退款不存在");
        }

        Integer newStatus;
        if (req.getResult() != null && req.getResult() == 1) {
            newStatus = 5;
        } else {
            newStatus = 6;
        }

        refundMapper.update(null, new LambdaUpdateWrapper<Refund>()
                .eq(Refund::getRefundId, req.getRefundId())
                .set(Refund::getArbitrateResult, req.getResult())
                .set(Refund::getArbitrateReason, req.getReason())
                .set(Refund::getStatus, newStatus)
                .set(Refund::getUpdateTime, LocalDateTime.now()));
    }

    private List<String> readJsonArray(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            return List.of();
        }
    }

    private String typeText(Integer type) {
        if (type == null) {
            return "未知";
        }
        return switch (type) {
            case 1 -> "仅退款";
            case 2 -> "退货退款";
            default -> "未知";
        };
    }

    private String statusText(Integer status) {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case 1 -> "待处理";
            case 2 -> "已同意";
            case 3 -> "已拒绝";
            case 4 -> "退款中";
            case 5 -> "已完成";
            case 6 -> "已关闭";
            default -> "未知";
        };
    }

    private String maskName(String name) {
        if (name == null || name.isBlank()) {
            return "用户";
        }
        if (name.length() <= 1) {
            return name + "***";
        }
        return name.charAt(0) + "***";
    }
}
