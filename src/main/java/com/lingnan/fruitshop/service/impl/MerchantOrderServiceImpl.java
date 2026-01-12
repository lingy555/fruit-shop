package com.lingnan.fruitshop.service.impl;

import com.lingnan.fruitshop.service.MerchantOrderService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lingnan.fruitshop.common.exception.BizException;
import com.lingnan.fruitshop.dto.customer.common.vo.PageResponse;
import com.lingnan.fruitshop.dto.merchant.order.*;
import com.lingnan.fruitshop.dto.merchant.order.vo.MerchantOrderDetailResponse;
import com.lingnan.fruitshop.dto.merchant.order.vo.MerchantOrderListResponse;
import com.lingnan.fruitshop.entity.*;
import com.lingnan.fruitshop.mapper.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class MerchantOrderServiceImpl implements MerchantOrderService {

    private static final DateTimeFormatter DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final int C_STATUS_WAIT_PAY = 1;
    private static final int C_STATUS_WAIT_DELIVER = 2;
    private static final int C_STATUS_WAIT_RECEIVE = 3;
    private static final int C_STATUS_WAIT_COMMENT = 4;
    private static final int C_STATUS_FINISHED = 5;
    private static final int C_STATUS_CANCELED = 6;

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderLogisticsMapper orderLogisticsMapper;
    private final UserAccountMapper userAccountMapper;
    private final ObjectMapper objectMapper;

    public MerchantOrderServiceImpl(OrderMapper orderMapper,
                               OrderItemMapper orderItemMapper,
                               OrderLogisticsMapper orderLogisticsMapper,
                               UserAccountMapper userAccountMapper,
                               ObjectMapper objectMapper) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.orderLogisticsMapper = orderLogisticsMapper;
        this.userAccountMapper = userAccountMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public MerchantOrderListResponse list(long shopId, int page, int pageSize, Integer status, String keyword, String startTime, String endTime) {
        LambdaQueryWrapper<Order> qw = new LambdaQueryWrapper<Order>()
                .eq(Order::getShopId, shopId);

        Integer internalStatus = toInternalStatus(status);
        if (internalStatus != null) {
            qw.eq(Order::getStatus, internalStatus);
        }

        if (keyword != null && !keyword.isBlank()) {
            qw.and(w -> w.like(Order::getOrderNo, keyword)
                    .or()
                    .like(Order::getReceiverName, keyword)
                    .or()
                    .like(Order::getReceiverPhone, keyword));
        }

        if (startTime != null && !startTime.isBlank()) {
            qw.ge(Order::getCreateTime, startTime);
        }
        if (endTime != null && !endTime.isBlank()) {
            qw.le(Order::getCreateTime, endTime);
        }

        qw.orderByDesc(Order::getCreateTime);

        Page<Order> p = new Page<>(page, pageSize);
        Page<Order> result = orderMapper.selectPage(p, qw);

        if (result.getRecords().isEmpty()) {
            return new MerchantOrderListResponse(0, statusCount(shopId), List.of());
        }

        Set<Long> orderIds = new HashSet<>();
        Set<Long> userIds = new HashSet<>();
        for (Order o : result.getRecords()) {
            orderIds.add(o.getOrderId());
            userIds.add(o.getUserId());
        }

        Map<Long, UserAccount> userMap = new HashMap<>();
        for (UserAccount u : userAccountMapper.selectBatchIds(userIds)) {
            userMap.put(u.getUserId(), u);
        }

        Map<Long, List<OrderItem>> itemsMap = new HashMap<>();
        for (OrderItem oi : orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>().in(OrderItem::getOrderId, orderIds))) {
            itemsMap.computeIfAbsent(oi.getOrderId(), k -> new ArrayList<>()).add(oi);
        }

        List<MerchantOrderListResponse.OrderItem> list = result.getRecords().stream().map(o -> {
            UserAccount user = userMap.get(o.getUserId());
            String username = user == null ? o.getReceiverName() : (user.getNickname() != null ? user.getNickname() : user.getUsername());

            MerchantOrderListResponse.Address address = new MerchantOrderListResponse.Address(
                    o.getReceiverName(),
                    o.getReceiverPhone(),
                    (o.getProvince() == null ? "" : o.getProvince()) + (o.getCity() == null ? "" : o.getCity()) + (o.getDistrict() == null ? "" : o.getDistrict()) + (o.getAddressDetail() == null ? "" : o.getAddressDetail())
            );

            List<MerchantOrderListResponse.Item> items = itemsMap.getOrDefault(o.getOrderId(), List.of()).stream().map(oi -> new MerchantOrderListResponse.Item(
                    oi.getOrderItemId(),
                    oi.getProductId(),
                    oi.getSkuId(),
                    oi.getProductName(),
                    oi.getImage(),
                    oi.getPrice(),
                    oi.getSpecs(),
                    oi.getQuantity(),
                    oi.getSubtotal()
            )).toList();

            boolean canDeliver = o.getStatus() != null && o.getStatus() == C_STATUS_WAIT_DELIVER;
            boolean canCancel = false;

            return new MerchantOrderListResponse.OrderItem(
                    o.getOrderId(),
                    o.getOrderNo(),
                    o.getUserId(),
                    username,
                    toMerchantStatus(o.getStatus()),
                    merchantStatusText(o.getStatus()),
                    o.getTotalAmount(),
                    o.getShippingFee(),
                    o.getActualAmount(),
                    o.getPaymentMethod(),
                    paymentMethodText(o.getPaymentMethod()),
                    o.getRemark(),
                    o.getCreateTime() == null ? null : o.getCreateTime().format(DATETIME),
                    o.getPayTime() == null ? null : o.getPayTime().format(DATETIME),
                    address,
                    items,
                    canDeliver,
                    canCancel
            );
        }).toList();

        return new MerchantOrderListResponse(result.getTotal(), statusCount(shopId), list);
    }

    @Override
    public MerchantOrderDetailResponse detail(long shopId, long orderId) {
        Order o = orderMapper.selectOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getShopId, shopId)
                .eq(Order::getOrderId, orderId)
                .last("limit 1"));
        if (o == null) {
            throw BizException.notFound("订单不存在");
        }

        UserAccount user = userAccountMapper.selectById(o.getUserId());
        String username = user == null ? o.getReceiverName() : (user.getNickname() != null ? user.getNickname() : user.getUsername());
        String userPhone = user == null ? o.getReceiverPhone() : user.getPhone();

        MerchantOrderDetailResponse.Address address = new MerchantOrderDetailResponse.Address(
                o.getReceiverName(),
                o.getReceiverPhone(),
                o.getProvince(),
                o.getCity(),
                o.getDistrict(),
                o.getAddressDetail()
        );

        List<OrderItem> orderItems = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, orderId)
                .orderByAsc(OrderItem::getOrderItemId));

        List<MerchantOrderDetailResponse.Item> items = orderItems.stream().map(oi -> {
            BigDecimal cost = oi.getCostPrice() == null ? BigDecimal.ZERO : oi.getCostPrice();
            int qty = oi.getQuantity() == null ? 0 : oi.getQuantity();
            BigDecimal profit = oi.getSubtotal() == null ? BigDecimal.ZERO : oi.getSubtotal().subtract(cost.multiply(BigDecimal.valueOf(qty)));
            return new MerchantOrderDetailResponse.Item(
                    oi.getOrderItemId(),
                    oi.getProductId(),
                    oi.getSkuId(),
                    oi.getProductName(),
                    oi.getImage(),
                    oi.getPrice(),
                    oi.getSpecs(),
                    oi.getQuantity(),
                    oi.getSubtotal(),
                    oi.getCostPrice(),
                    profit
            );
        }).toList();

        List<MerchantOrderDetailResponse.Timeline> timeline = buildTimeline(o);

        return new MerchantOrderDetailResponse(
                o.getOrderId(),
                o.getOrderNo(),
                o.getUserId(),
                username,
                userPhone,
                toMerchantStatus(o.getStatus()),
                merchantStatusText(o.getStatus()),
                o.getTotalAmount(),
                o.getShippingFee(),
                o.getCouponDiscount(),
                o.getActualAmount(),
                o.getPaymentMethod(),
                paymentMethodText(o.getPaymentMethod()),
                o.getRemark(),
                o.getCreateTime() == null ? null : o.getCreateTime().format(DATETIME),
                o.getPayTime() == null ? null : o.getPayTime().format(DATETIME),
                o.getDeliverTime() == null ? null : o.getDeliverTime().format(DATETIME),
                o.getReceiveTime() == null ? null : o.getReceiveTime().format(DATETIME),
                o.getFinishTime() == null ? null : o.getFinishTime().format(DATETIME),
                address,
                items,
                timeline
        );
    }

    @Override
    public void deliver(long shopId, MerchantOrderDeliverRequest req) {
        Order o = orderMapper.selectOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getShopId, shopId)
                .eq(Order::getOrderId, req.getOrderId())
                .last("limit 1"));
        if (o == null) {
            throw BizException.notFound("订单不存在");
        }
        if (o.getStatus() == null || o.getStatus() != C_STATUS_WAIT_DELIVER) {
            throw BizException.badRequest("当前订单不可发货");
        }

        LocalDateTime now = LocalDateTime.now();
        orderMapper.update(null, new LambdaUpdateWrapper<Order>()
                .eq(Order::getOrderId, o.getOrderId())
                .eq(Order::getShopId, shopId)
                .set(Order::getStatus, C_STATUS_WAIT_RECEIVE)
                .set(Order::getDeliverTime, now));

        OrderLogistics logistics = orderLogisticsMapper.selectOne(new LambdaQueryWrapper<OrderLogistics>()
                .eq(OrderLogistics::getOrderId, o.getOrderId())
                .last("limit 1"));

        String tracesJson = buildTracesJson(now, "商家已发货");
        if (logistics == null) {
            logistics = new OrderLogistics();
            logistics.setOrderId(o.getOrderId());
            logistics.setExpressCompany(req.getExpressCompany());
            logistics.setExpressNo(req.getExpressNo());
            logistics.setCurrentStatus("已发货");
            logistics.setTracesJson(tracesJson);
            orderLogisticsMapper.insert(logistics);
        } else {
            logistics.setExpressCompany(req.getExpressCompany());
            logistics.setExpressNo(req.getExpressNo());
            logistics.setCurrentStatus("已发货");
            logistics.setTracesJson(tracesJson);
            orderLogisticsMapper.updateById(logistics);
        }
    }

    @Override
    public void batchDeliver(long shopId, MerchantOrderBatchDeliverRequest req) {
        for (MerchantOrderDeliverRequest d : req.getDeliverList()) {
            deliver(shopId, d);
        }
    }

    @Override
    public void updateShippingFee(long shopId, MerchantOrderUpdateShippingFeeRequest req) {
        Order o = orderMapper.selectOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getShopId, shopId)
                .eq(Order::getOrderId, req.getOrderId())
                .last("limit 1"));
        if (o == null) {
            throw BizException.notFound("订单不存在");
        }
        if (o.getStatus() == null || o.getStatus() != C_STATUS_WAIT_DELIVER) {
            throw BizException.badRequest("当前订单不可修改运费");
        }

        BigDecimal total = o.getTotalAmount() == null ? BigDecimal.ZERO : o.getTotalAmount();
        BigDecimal couponDiscount = o.getCouponDiscount() == null ? BigDecimal.ZERO : o.getCouponDiscount();
        BigDecimal pointsDiscount = o.getPointsDiscount() == null ? BigDecimal.ZERO : o.getPointsDiscount();
        BigDecimal shipping = req.getShippingFee();
        BigDecimal actual = total.add(shipping).subtract(couponDiscount).subtract(pointsDiscount);
        if (actual.compareTo(BigDecimal.ZERO) < 0) {
            actual = BigDecimal.ZERO;
        }

        orderMapper.update(null, new LambdaUpdateWrapper<Order>()
                .eq(Order::getOrderId, o.getOrderId())
                .eq(Order::getShopId, shopId)
                .set(Order::getShippingFee, shipping)
                .set(Order::getActualAmount, actual));
    }

    @Override
    public void updateRemark(long shopId, MerchantOrderUpdateRemarkRequest req) {
        Order o = orderMapper.selectOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getShopId, shopId)
                .eq(Order::getOrderId, req.getOrderId())
                .last("limit 1"));
        if (o == null) {
            throw BizException.notFound("订单不存在");
        }
        orderMapper.update(null, new LambdaUpdateWrapper<Order>()
                .eq(Order::getOrderId, o.getOrderId())
                .eq(Order::getShopId, shopId)
                .set(Order::getMerchantRemark, req.getMerchantRemark()));
    }

    @Override
    public Map<String, Object> export(long shopId, MerchantOrderExportRequest req) {
        Map<String, Object> res = new HashMap<>();
        res.put("url", "export://merchant/orders?shopId=" + shopId);
        return res;
    }

    private MerchantOrderListResponse.StatusCount statusCount(long shopId) {
        long waitDeliver = orderMapper.selectCount(new LambdaQueryWrapper<Order>().eq(Order::getShopId, shopId).eq(Order::getStatus, C_STATUS_WAIT_DELIVER));
        long waitReceive = orderMapper.selectCount(new LambdaQueryWrapper<Order>().eq(Order::getShopId, shopId).eq(Order::getStatus, C_STATUS_WAIT_RECEIVE));
        long finished = orderMapper.selectCount(new LambdaQueryWrapper<Order>().eq(Order::getShopId, shopId).eq(Order::getStatus, C_STATUS_FINISHED));
        long refunding = 0;
        return new MerchantOrderListResponse.StatusCount(waitDeliver, waitReceive, finished, refunding);
    }

    private Integer toInternalStatus(Integer merchantStatus) {
        if (merchantStatus == null) {
            return null;
        }
        return switch (merchantStatus) {
            case 1 -> C_STATUS_WAIT_DELIVER;
            case 2 -> C_STATUS_WAIT_RECEIVE;
            case 3 -> C_STATUS_FINISHED;
            case 4 -> C_STATUS_CANCELED;
            default -> null;
        };
    }

    private int toMerchantStatus(Integer internalStatus) {
        if (internalStatus == null) {
            return 0;
        }
        return switch (internalStatus) {
            case C_STATUS_WAIT_DELIVER -> 1;
            case C_STATUS_WAIT_RECEIVE -> 2;
            case C_STATUS_FINISHED -> 3;
            case C_STATUS_CANCELED -> 4;
            default -> 0;
        };
    }

    private String merchantStatusText(Integer internalStatus) {
        if (internalStatus == null) {
            return "未知";
        }
        return switch (internalStatus) {
            case C_STATUS_WAIT_DELIVER -> "待发货";
            case C_STATUS_WAIT_RECEIVE -> "待收货";
            case C_STATUS_FINISHED -> "已完成";
            case C_STATUS_CANCELED -> "已取消";
            case C_STATUS_WAIT_PAY -> "待付款";
            case C_STATUS_WAIT_COMMENT -> "待评价";
            default -> "未知";
        };
    }

    private String paymentMethodText(String method) {
        if (method == null) {
            return null;
        }
        return switch (method) {
            case "alipay" -> "支付宝";
            case "wechat" -> "微信";
            case "balance" -> "余额";
            default -> method;
        };
    }

    private String buildTracesJson(LocalDateTime now, String status) {
        try {
            List<Map<String, Object>> traces = new ArrayList<>();
            Map<String, Object> item = new HashMap<>();
            item.put("time", now.format(DATETIME));
            item.put("status", status);
            traces.add(item);
            return objectMapper.writeValueAsString(traces);
        } catch (Exception e) {
            return null;
        }
    }

    private List<MerchantOrderDetailResponse.Timeline> buildTimeline(Order o) {
        List<MerchantOrderDetailResponse.Timeline> list = new ArrayList<>();
        if (o.getFinishTime() != null) {
            list.add(new MerchantOrderDetailResponse.Timeline(o.getFinishTime().format(DATETIME), "订单完成"));
        }
        if (o.getReceiveTime() != null) {
            list.add(new MerchantOrderDetailResponse.Timeline(o.getReceiveTime().format(DATETIME), "买家确认收货"));
        }
        if (o.getDeliverTime() != null) {
            list.add(new MerchantOrderDetailResponse.Timeline(o.getDeliverTime().format(DATETIME), "商家已发货"));
        }
        if (o.getPayTime() != null) {
            list.add(new MerchantOrderDetailResponse.Timeline(o.getPayTime().format(DATETIME), "买家已付款"));
        }
        if (o.getCreateTime() != null) {
            list.add(new MerchantOrderDetailResponse.Timeline(o.getCreateTime().format(DATETIME), "买家下单"));
        }
        return list;
    }
}
