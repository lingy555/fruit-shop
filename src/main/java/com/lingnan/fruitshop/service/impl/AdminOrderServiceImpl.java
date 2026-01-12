package com.lingnan.fruitshop.service.impl;

import com.lingnan.fruitshop.service.AdminOrderService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lingnan.fruitshop.common.exception.BizException;
import com.lingnan.fruitshop.dto.admin.order.vo.AdminOrderDetailResponse;
import com.lingnan.fruitshop.dto.admin.order.vo.AdminOrderListResponse;
import com.lingnan.fruitshop.dto.admin.order.vo.AdminOrderStatisticsResponse;
import com.lingnan.fruitshop.entity.*;
import com.lingnan.fruitshop.mapper.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class AdminOrderServiceImpl implements AdminOrderService {

    private static final DateTimeFormatter DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderLogisticsMapper orderLogisticsMapper;
    private final UserAccountMapper userAccountMapper;
    private final ShopMapper shopMapper;
    private final RefundMapper refundMapper;
    private final ObjectMapper objectMapper;

    public AdminOrderServiceImpl(OrderMapper orderMapper,
                             OrderItemMapper orderItemMapper,
                             OrderLogisticsMapper orderLogisticsMapper,
                             UserAccountMapper userAccountMapper,
                             ShopMapper shopMapper,
                             RefundMapper refundMapper,
                             ObjectMapper objectMapper) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.orderLogisticsMapper = orderLogisticsMapper;
        this.userAccountMapper = userAccountMapper;
        this.shopMapper = shopMapper;
        this.refundMapper = refundMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public AdminOrderListResponse list(int page, int pageSize, String orderNo, Integer status, Long shopId, Long userId, String keyword, String startTime, String endTime) {
        LambdaQueryWrapper<Order> qw = new LambdaQueryWrapper<>();
        if (orderNo != null && !orderNo.isBlank()) {
            qw.like(Order::getOrderNo, orderNo);
        }
        if (status != null) {
            qw.eq(Order::getStatus, status);
        }
        if (shopId != null) {
            qw.eq(Order::getShopId, shopId);
        }
        if (userId != null) {
            qw.eq(Order::getUserId, userId);
        }
        if (keyword != null && !keyword.isBlank()) {
            qw.and(w -> w.like(Order::getReceiverName, keyword)
                    .or().like(Order::getReceiverPhone, keyword));
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

        AdminOrderListResponse.StatusCount statusCount = statusCount();
        if (result.getRecords().isEmpty()) {
            return new AdminOrderListResponse(0, statusCount, List.of());
        }

        Set<Long> userIds = new HashSet<>();
        Set<Long> shopIds = new HashSet<>();
        Set<Long> orderIds = new HashSet<>();
        for (Order o : result.getRecords()) {
            if (o.getUserId() != null) userIds.add(o.getUserId());
            if (o.getShopId() != null) shopIds.add(o.getShopId());
            if (o.getOrderId() != null) orderIds.add(o.getOrderId());
        }

        Set<Long> refundOrderIds = new HashSet<>();
        if (!orderIds.isEmpty()) {
            for (Refund r : refundMapper.selectList(new LambdaQueryWrapper<Refund>().in(Refund::getOrderId, orderIds))) {
                if (r.getOrderId() != null && r.getStatus() != null && (r.getStatus() == 1 || r.getStatus() == 2 || r.getStatus() == 3 || r.getStatus() == 4)) {
                    refundOrderIds.add(r.getOrderId());
                }
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

        List<AdminOrderListResponse.Item> list = result.getRecords().stream().map(o -> {
            UserAccount u = userMap.get(o.getUserId());
            Shop s = shopMap.get(o.getShopId());
            String username = u == null ? o.getReceiverName() : (u.getNickname() != null ? u.getNickname() : u.getUsername());

            Integer displayStatus = o.getStatus();
            String statusText = orderStatusText(o.getStatus());
            if (refundOrderIds.contains(o.getOrderId())) {
                displayStatus = 7;
                statusText = "退款中";
            }

            return new AdminOrderListResponse.Item(
                    o.getOrderId(),
                    o.getOrderNo(),
                    o.getUserId(),
                    username,
                    o.getShopId(),
                    s == null ? null : s.getShopName(),
                    displayStatus,
                    statusText,
                    o.getTotalAmount(),
                    o.getActualAmount(),
                    o.getPaymentMethod(),
                    format(o.getCreateTime()),
                    format(o.getPayTime())
            );
        }).toList();

        return new AdminOrderListResponse(result.getTotal(), statusCount, list);
    }

    @Override
    public AdminOrderDetailResponse detail(long orderId) {
        Order o = orderMapper.selectById(orderId);
        if (o == null) {
            throw BizException.notFound("订单不存在");
        }

        UserAccount user = o.getUserId() == null ? null : userAccountMapper.selectById(o.getUserId());
        Shop shop = o.getShopId() == null ? null : shopMapper.selectById(o.getShopId());

        String username = user == null ? o.getReceiverName() : (user.getNickname() != null ? user.getNickname() : user.getUsername());
        String userPhone = user == null ? o.getReceiverPhone() : user.getPhone();

        AdminOrderDetailResponse.Address address = new AdminOrderDetailResponse.Address(
                o.getReceiverName(),
                o.getReceiverPhone(),
                o.getProvince(),
                o.getCity(),
                o.getDistrict(),
                o.getAddressDetail()
        );

        OrderLogistics logistics = orderLogisticsMapper.selectOne(new LambdaQueryWrapper<OrderLogistics>()
                .eq(OrderLogistics::getOrderId, orderId)
                .last("limit 1"));

        AdminOrderDetailResponse.Logistics logisticsVo = null;
        if (logistics != null) {
            logisticsVo = new AdminOrderDetailResponse.Logistics(
                    logistics.getExpressCompany(),
                    logistics.getExpressNo(),
                    logistics.getCurrentStatus(),
                    readTraces(logistics.getTracesJson())
            );
        }

        List<OrderItem> orderItems = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, orderId)
                .orderByAsc(OrderItem::getOrderItemId));

        List<AdminOrderDetailResponse.Item> items = orderItems.stream().map(oi -> {
            BigDecimal cost = oi.getCostPrice() == null ? BigDecimal.ZERO : oi.getCostPrice();
            int qty = oi.getQuantity() == null ? 0 : oi.getQuantity();
            BigDecimal profit = oi.getSubtotal() == null ? BigDecimal.ZERO : oi.getSubtotal().subtract(cost.multiply(BigDecimal.valueOf(qty)));
            return new AdminOrderDetailResponse.Item(
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

        List<AdminOrderDetailResponse.Timeline> timeline = buildTimeline(o);

        return new AdminOrderDetailResponse(
                o.getOrderId(),
                o.getOrderNo(),
                o.getUserId(),
                username,
                userPhone,
                o.getShopId(),
                shop == null ? null : shop.getShopName(),
                o.getStatus(),
                orderStatusText(o.getStatus()),
                o.getTotalAmount(),
                o.getShippingFee(),
                o.getCouponDiscount(),
                o.getPointsDiscount(),
                o.getActualAmount(),
                o.getPaymentMethod(),
                paymentMethodText(o.getPaymentMethod()),
                o.getRemark(),
                o.getMerchantRemark(),
                format(o.getCreateTime()),
                format(o.getPayTime()),
                format(o.getDeliverTime()),
                format(o.getReceiveTime()),
                format(o.getFinishTime()),
                format(o.getCancelTime()),
                address,
                logisticsVo,
                items,
                timeline
        );
    }

    @Override
    public AdminOrderStatisticsResponse statistics(String dateType, String startDate, String endDate) {
        LocalDateTime start = null;
        LocalDateTime end = null;
        LocalDate today = LocalDate.now();

        if (dateType != null && !dateType.isBlank()) {
            if ("today".equalsIgnoreCase(dateType)) {
                start = today.atStartOfDay();
                end = today.plusDays(1).atStartOfDay();
            } else if ("week".equalsIgnoreCase(dateType)) {
                start = today.minusDays(6).atStartOfDay();
                end = today.plusDays(1).atStartOfDay();
            } else if ("month".equalsIgnoreCase(dateType)) {
                start = today.withDayOfMonth(1).atStartOfDay();
                end = today.plusDays(1).atStartOfDay();
            } else if ("year".equalsIgnoreCase(dateType)) {
                start = today.withDayOfYear(1).atStartOfDay();
                end = today.plusDays(1).atStartOfDay();
            }
        }

        if (start == null && startDate != null && !startDate.isBlank()) {
            start = LocalDate.parse(startDate).atStartOfDay();
        }
        if (end == null && endDate != null && !endDate.isBlank()) {
            end = LocalDate.parse(endDate).plusDays(1).atStartOfDay();
        }

        LambdaQueryWrapper<Order> qw = new LambdaQueryWrapper<>();
        if (start != null) {
            qw.ge(Order::getCreateTime, start);
        }
        if (end != null) {
            qw.lt(Order::getCreateTime, end);
        }
        qw.orderByAsc(Order::getCreateTime);

        List<Order> orders = orderMapper.selectList(qw);

        Map<String, AdminOrderStatisticsResponse.TrendItem> trendMap = new LinkedHashMap<>();
        long totalCount = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (Order o : orders) {
            String date = o.getCreateTime() == null ? null : o.getCreateTime().toLocalDate().toString();
            if (date == null) {
                continue;
            }
            AdminOrderStatisticsResponse.TrendItem t = trendMap.get(date);
            if (t == null) {
                t = new AdminOrderStatisticsResponse.TrendItem(date, 0, BigDecimal.ZERO);
                trendMap.put(date, t);
            }
            t.setOrderCount(t.getOrderCount() + 1);
            BigDecimal amt = o.getActualAmount() == null ? BigDecimal.ZERO : o.getActualAmount();
            t.setOrderAmount(t.getOrderAmount().add(amt));

            totalCount += 1;
            totalAmount = totalAmount.add(amt);
        }

        AdminOrderStatisticsResponse.Summary summary = new AdminOrderStatisticsResponse.Summary(totalCount, totalAmount);
        return new AdminOrderStatisticsResponse(summary, new ArrayList<>(trendMap.values()));
    }

    private AdminOrderListResponse.StatusCount statusCount() {
        long waitPay = safeLong(orderMapper.selectCount(new LambdaQueryWrapper<Order>().eq(Order::getStatus, 1)));
        long waitDeliver = safeLong(orderMapper.selectCount(new LambdaQueryWrapper<Order>().eq(Order::getStatus, 2)));
        long waitReceive = safeLong(orderMapper.selectCount(new LambdaQueryWrapper<Order>().eq(Order::getStatus, 3)));
        long finished = safeLong(orderMapper.selectCount(new LambdaQueryWrapper<Order>().eq(Order::getStatus, 5)));
        long cancelled = safeLong(orderMapper.selectCount(new LambdaQueryWrapper<Order>().eq(Order::getStatus, 6)));

        long refunding = safeLong(refundMapper.selectCount(new LambdaQueryWrapper<Refund>().in(Refund::getStatus, List.of(1, 2, 3, 4))));

        return new AdminOrderListResponse.StatusCount(waitPay, waitDeliver, waitReceive, finished, cancelled, refunding);
    }

    private long safeLong(Long v) {
        return v == null ? 0 : v;
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
            case 7 -> "退款中";
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

    private String format(LocalDateTime dt) {
        return dt == null ? null : DATETIME.format(dt);
    }

    private List<AdminOrderDetailResponse.Trace> readTraces(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            List<Map<String, Object>> list = objectMapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {
            });
            List<AdminOrderDetailResponse.Trace> res = new ArrayList<>();
            for (Map<String, Object> m : list) {
                res.add(new AdminOrderDetailResponse.Trace(
                        m.get("time") == null ? null : String.valueOf(m.get("time")),
                        m.get("status") == null ? null : String.valueOf(m.get("status"))
                ));
            }
            return res;
        } catch (Exception e) {
            return List.of();
        }
    }

    private List<AdminOrderDetailResponse.Timeline> buildTimeline(Order o) {
        List<AdminOrderDetailResponse.Timeline> list = new ArrayList<>();
        if (o.getFinishTime() != null) {
            list.add(new AdminOrderDetailResponse.Timeline(o.getFinishTime().format(DATETIME), "订单完成"));
        }
        if (o.getReceiveTime() != null) {
            list.add(new AdminOrderDetailResponse.Timeline(o.getReceiveTime().format(DATETIME), "买家确认收货"));
        }
        if (o.getDeliverTime() != null) {
            list.add(new AdminOrderDetailResponse.Timeline(o.getDeliverTime().format(DATETIME), "商家已发货"));
        }
        if (o.getPayTime() != null) {
            list.add(new AdminOrderDetailResponse.Timeline(o.getPayTime().format(DATETIME), "买家已付款"));
        }
        if (o.getCancelTime() != null) {
            list.add(new AdminOrderDetailResponse.Timeline(o.getCancelTime().format(DATETIME), "订单取消"));
        }
        if (o.getCreateTime() != null) {
            list.add(new AdminOrderDetailResponse.Timeline(o.getCreateTime().format(DATETIME), "买家下单"));
        }
        return list;
    }
}
