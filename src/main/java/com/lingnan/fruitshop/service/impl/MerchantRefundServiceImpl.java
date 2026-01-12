package com.lingnan.fruitshop.service.impl;

import com.lingnan.fruitshop.service.MerchantRefundService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lingnan.fruitshop.common.exception.BizException;
import com.lingnan.fruitshop.dto.merchant.refund.*;
import com.lingnan.fruitshop.dto.merchant.refund.vo.MerchantRefundDetailResponse;
import com.lingnan.fruitshop.dto.merchant.refund.vo.MerchantRefundListResponse;
import com.lingnan.fruitshop.entity.*;
import com.lingnan.fruitshop.mapper.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class MerchantRefundServiceImpl implements MerchantRefundService {

    private static final DateTimeFormatter DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final RefundMapper refundMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final UserAccountMapper userAccountMapper;
    private final ObjectMapper objectMapper;

    public MerchantRefundServiceImpl(RefundMapper refundMapper,
                                OrderMapper orderMapper,
                                OrderItemMapper orderItemMapper,
                                UserAccountMapper userAccountMapper,
                                ObjectMapper objectMapper) {
        this.refundMapper = refundMapper;
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.userAccountMapper = userAccountMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public MerchantRefundListResponse list(long shopId, int page, int pageSize, Integer status, String keyword) {
        LambdaQueryWrapper<Refund> qw = new LambdaQueryWrapper<Refund>()
                .eq(Refund::getShopId, shopId);

        if (status != null) {
            qw.eq(Refund::getStatus, status);
        }

        if (keyword != null && !keyword.isBlank()) {
            qw.and(w -> w.like(Refund::getRefundNo, keyword)
                    .or()
                    .like(Refund::getReason, keyword)
                    .or()
                    .like(Refund::getDescription, keyword));
        }

        qw.orderByDesc(Refund::getCreateTime);

        Page<Refund> p = new Page<>(page, pageSize);
        Page<Refund> result = refundMapper.selectPage(p, qw);

        if (result.getRecords().isEmpty()) {
            return new MerchantRefundListResponse(0, statusCount(shopId), List.of());
        }

        Set<Long> orderIds = new HashSet<>();
        Set<Long> userIds = new HashSet<>();
        Set<Long> orderItemIds = new HashSet<>();
        for (Refund r : result.getRecords()) {
            if (r.getOrderId() != null) orderIds.add(r.getOrderId());
            if (r.getUserId() != null) userIds.add(r.getUserId());
            if (r.getOrderItemId() != null) orderItemIds.add(r.getOrderItemId());
        }

        Map<Long, Order> orderMap = new HashMap<>();
        for (Order o : orderMapper.selectBatchIds(orderIds)) {
            orderMap.put(o.getOrderId(), o);
        }

        Map<Long, UserAccount> userMap = new HashMap<>();
        for (UserAccount u : userAccountMapper.selectBatchIds(userIds)) {
            userMap.put(u.getUserId(), u);
        }

        Map<Long, OrderItem> orderItemMap = new HashMap<>();
        for (OrderItem oi : orderItemMapper.selectBatchIds(orderItemIds)) {
            orderItemMap.put(oi.getOrderItemId(), oi);
        }

        List<MerchantRefundListResponse.Item> list = result.getRecords().stream().map(r -> {
            Order o = orderMap.get(r.getOrderId());
            UserAccount u = userMap.get(r.getUserId());
            OrderItem oi = r.getOrderItemId() == null ? null : orderItemMap.get(r.getOrderItemId());

            String username = u == null ? "用户" : maskName(u.getNickname() != null ? u.getNickname() : u.getUsername());

            MerchantRefundListResponse.Product product = null;
            if (oi != null) {
                product = new MerchantRefundListResponse.Product(oi.getProductName(), oi.getImage(), oi.getPrice(), oi.getQuantity());
            }

            return new MerchantRefundListResponse.Item(
                    r.getRefundId(),
                    r.getRefundNo(),
                    r.getOrderId(),
                    o == null ? null : o.getOrderNo(),
                    r.getUserId(),
                    username,
                    r.getType(),
                    typeText(r.getType()),
                    r.getReason(),
                    r.getDescription(),
                    readJsonArray(r.getImagesJson()),
                    r.getRefundAmount(),
                    r.getStatus(),
                    statusText(r.getStatus()),
                    product,
                    r.getCreateTime() == null ? null : r.getCreateTime().format(DATETIME)
            );
        }).toList();

        return new MerchantRefundListResponse(result.getTotal(), statusCount(shopId), list);
    }

    @Override
    public MerchantRefundDetailResponse detail(long shopId, long refundId) {
        Refund r = refundMapper.selectOne(new LambdaQueryWrapper<Refund>()
                .eq(Refund::getShopId, shopId)
                .eq(Refund::getRefundId, refundId)
                .last("limit 1"));
        if (r == null) {
            throw BizException.notFound("退款不存在");
        }

        Order o = r.getOrderId() == null ? null : orderMapper.selectById(r.getOrderId());
        UserAccount u = r.getUserId() == null ? null : userAccountMapper.selectById(r.getUserId());
        OrderItem oi = r.getOrderItemId() == null ? null : orderItemMapper.selectById(r.getOrderItemId());

        String username = u == null ? "用户" : maskName(u.getNickname() != null ? u.getNickname() : u.getUsername());
        String userPhone = u == null ? null : u.getPhone();

        MerchantRefundDetailResponse.Product product = null;
        if (oi != null) {
            product = new MerchantRefundDetailResponse.Product(oi.getProductId(), oi.getProductName(), oi.getImage(), oi.getSpecs(), oi.getPrice(), oi.getQuantity());
        }

        List<MerchantRefundDetailResponse.Timeline> timeline = List.of(new MerchantRefundDetailResponse.Timeline(
                r.getCreateTime() == null ? null : r.getCreateTime().format(DATETIME),
                "买家申请退款",
                r.getReason()
        ));

        return new MerchantRefundDetailResponse(
                r.getRefundId(),
                r.getRefundNo(),
                r.getOrderId(),
                o == null ? null : o.getOrderNo(),
                r.getOrderItemId(),
                r.getUserId(),
                username,
                userPhone,
                r.getType(),
                typeText(r.getType()),
                r.getReason(),
                r.getDescription(),
                readJsonArray(r.getImagesJson()),
                r.getRefundAmount(),
                r.getStatus(),
                statusText(r.getStatus()),
                product,
                timeline,
                r.getMerchantReply(),
                r.getCreateTime() == null ? null : r.getCreateTime().format(DATETIME)
        );
    }

    @Override
    public void agree(long shopId, MerchantRefundAgreeRequest req) {
        Refund r = mustOwn(shopId, req.getRefundId());
        int updated = refundMapper.update(null, new LambdaUpdateWrapper<Refund>()
                .eq(Refund::getRefundId, r.getRefundId())
                .eq(Refund::getShopId, shopId)
                .eq(Refund::getStatus, 1)
                .isNull(Refund::getArbitrateResult)
                .set(Refund::getStatus, 4)
                .set(Refund::getMerchantReply, req.getMerchantRemark())
                .set(Refund::getUpdateTime, LocalDateTime.now()));
        if (updated <= 0) {
            throw BizException.badRequest("当前退款状态不可同意");
        }
    }

    @Override
    public void reject(long shopId, MerchantRefundRejectRequest req) {
        Refund r = mustOwn(shopId, req.getRefundId());
        int updated = refundMapper.update(null, new LambdaUpdateWrapper<Refund>()
                .eq(Refund::getRefundId, r.getRefundId())
                .eq(Refund::getShopId, shopId)
                .eq(Refund::getStatus, 1)
                .isNull(Refund::getArbitrateResult)
                .set(Refund::getStatus, 3)
                .set(Refund::getRejectReason, req.getRejectReason())
                .set(Refund::getImagesJson, writeJson(req.getImages()))
                .set(Refund::getUpdateTime, LocalDateTime.now()));
        if (updated <= 0) {
            throw BizException.badRequest("当前退款状态不可拒绝");
        }
    }

    @Override
    public void confirmReceive(long shopId, MerchantRefundConfirmReceiveRequest req) {
        Refund r = mustOwn(shopId, req.getRefundId());
        int updated = refundMapper.update(null, new LambdaUpdateWrapper<Refund>()
                .eq(Refund::getRefundId, r.getRefundId())
                .eq(Refund::getShopId, shopId)
                .eq(Refund::getStatus, 4)
                .isNull(Refund::getArbitrateResult)
                .set(Refund::getStatus, 5)
                .set(Refund::getUpdateTime, LocalDateTime.now()));
        if (updated <= 0) {
            throw BizException.badRequest("当前退款状态不可确认收货");
        }
    }

    private Refund mustOwn(long shopId, long refundId) {
        Refund r = refundMapper.selectOne(new LambdaQueryWrapper<Refund>()
                .eq(Refund::getShopId, shopId)
                .eq(Refund::getRefundId, refundId)
                .last("limit 1"));
        if (r == null) {
            throw BizException.notFound("退款不存在");
        }
        return r;
    }

    private MerchantRefundListResponse.StatusCount statusCount(long shopId) {
        long pending = refundMapper.selectCount(new LambdaQueryWrapper<Refund>().eq(Refund::getShopId, shopId).eq(Refund::getStatus, 1));
        long processing = refundMapper.selectCount(new LambdaQueryWrapper<Refund>().eq(Refund::getShopId, shopId).in(Refund::getStatus, List.of(2, 4)));
        long finished = refundMapper.selectCount(new LambdaQueryWrapper<Refund>().eq(Refund::getShopId, shopId).eq(Refund::getStatus, 5));
        return new MerchantRefundListResponse.StatusCount(pending, processing, finished);
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

    private String writeJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return null;
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
