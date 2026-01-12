package com.lingnan.fruitshop.service.impl;

import com.lingnan.fruitshop.service.MerchantStatisticsService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lingnan.fruitshop.dto.merchant.statistics.vo.*;
import com.lingnan.fruitshop.entity.*;
import com.lingnan.fruitshop.mapper.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class MerchantStatisticsServiceImpl implements MerchantStatisticsService {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductMapper productMapper;
    private final ProductCategoryMapper productCategoryMapper;
    private final RefundMapper refundMapper;
    private final CommentMapper commentMapper;
    private final UserAccountMapper userAccountMapper;

    public MerchantStatisticsServiceImpl(OrderMapper orderMapper,
                                    OrderItemMapper orderItemMapper,
                                    ProductMapper productMapper,
                                    ProductCategoryMapper productCategoryMapper,
                                    RefundMapper refundMapper,
                                    CommentMapper commentMapper,
                                    UserAccountMapper userAccountMapper) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.productMapper = productMapper;
        this.productCategoryMapper = productCategoryMapper;
        this.refundMapper = refundMapper;
        this.commentMapper = commentMapper;
        this.userAccountMapper = userAccountMapper;
    }

    @Override
    public MerchantOverviewResponse overview(long shopId) {
        DateRange today = dateRange("today", null, null);
        DateRange yesterday = new DateRange(today.start.minusDays(1), today.end.minusDays(1));

        List<Order> todayOrders = listOrders(shopId, today);
        List<Order> yesterdayOrders = listOrders(shopId, yesterday);

        MerchantOverviewResponse.TodayData todayData = new MerchantOverviewResponse.TodayData(
                todayOrders.size(),
                sumTotal(todayOrders),
                countPaid(todayOrders),
                sumPaid(todayOrders),
                0,
                0,
                countRefunds(shopId, today)
        );

        double orderCountRate = rate(todayOrders.size(), yesterdayOrders.size());
        double orderAmountRate = rate(sumTotal(todayOrders), sumTotal(yesterdayOrders));
        double visitCountRate = 0D;
        MerchantOverviewResponse.CompareYesterday compareYesterday = new MerchantOverviewResponse.CompareYesterday(orderCountRate, orderAmountRate, visitCountRate);

        long waitDeliverCount = orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                .eq(Order::getShopId, shopId)
                .eq(Order::getStatus, 2));

        long refundingCount = refundMapper.selectCount(new LambdaQueryWrapper<Refund>()
                .eq(Refund::getShopId, shopId)
                .in(Refund::getStatus, List.of(1, 4)));

        long lowStockCount = productMapper.selectCount(new LambdaQueryWrapper<Product>()
                .eq(Product::getShopId, shopId)
                .lt(Product::getStock, 10));

        long unreplyCommentCount = commentMapper.selectCount(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getShopId, shopId)
                .eq(Comment::getStatus, 1)
                .and(w -> w.isNull(Comment::getMerchantReplyContent).or().eq(Comment::getMerchantReplyContent, "")));

        MerchantOverviewResponse.PendingCount pendingCount = new MerchantOverviewResponse.PendingCount(waitDeliverCount, refundingCount, lowStockCount, unreplyCommentCount);

        return new MerchantOverviewResponse(todayData, compareYesterday, pendingCount);
    }

    @Override
    public MerchantSalesStatisticsResponse sales(long shopId, String dateType, String startDate, String endDate) {
        DateRange range = dateRange(dateType, startDate, endDate);

        List<Order> orders = listOrders(shopId, range);
        long totalOrderCount = orders.size();
        BigDecimal totalOrderAmount = sumTotal(orders);

        List<Order> paid = orders.stream().filter(this::isPaid).toList();
        long totalPayOrderCount = paid.size();
        BigDecimal totalPayOrderAmount = sumPaid(paid);

        BigDecimal avgOrderAmount = totalPayOrderCount == 0 ? BigDecimal.ZERO : totalPayOrderAmount.divide(BigDecimal.valueOf(totalPayOrderCount), 2, RoundingMode.HALF_UP);

        List<Refund> refunds = refundMapper.selectList(new LambdaQueryWrapper<Refund>()
                .eq(Refund::getShopId, shopId)
                .ge(Refund::getCreateTime, range.start)
                .le(Refund::getCreateTime, range.end));
        long refundCount = refunds.size();
        BigDecimal refundAmount = refunds.stream().map(r -> r.getRefundAmount() == null ? BigDecimal.ZERO : r.getRefundAmount()).reduce(BigDecimal.ZERO, BigDecimal::add);
        double refundRate = totalPayOrderCount == 0 ? 0D : (refundCount * 100.0 / totalPayOrderCount);

        List<MerchantSalesStatisticsResponse.TrendData> trendData = buildSalesTrend(range, orders);
        List<MerchantSalesStatisticsResponse.CategoryData> categoryData = buildCategoryData(shopId, range);

        return new MerchantSalesStatisticsResponse(
                totalOrderCount,
                totalOrderAmount,
                totalPayOrderCount,
                totalPayOrderAmount,
                avgOrderAmount,
                refundCount,
                refundAmount,
                refundRate,
                trendData,
                categoryData
        );
    }

    @Override
    public MerchantProductStatisticsResponse product(long shopId, String sortBy, Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10;
        }

        List<Product> top;
        if ("amount".equalsIgnoreCase(sortBy)) {
            top = productMapper.selectList(new LambdaQueryWrapper<Product>()
                    .eq(Product::getShopId, shopId)
                    .orderByDesc(Product::getSales)
                    .last("limit " + limit));
        } else if ("visitCount".equalsIgnoreCase(sortBy)) {
            top = productMapper.selectList(new LambdaQueryWrapper<Product>()
                    .eq(Product::getShopId, shopId)
                    .orderByDesc(Product::getViewCount)
                    .last("limit " + limit));
        } else {
            top = productMapper.selectList(new LambdaQueryWrapper<Product>()
                    .eq(Product::getShopId, shopId)
                    .orderByDesc(Product::getSales)
                    .last("limit " + limit));
        }

        List<MerchantProductStatisticsResponse.TopSalesProduct> topSales = top.stream().map(p -> {
            BigDecimal amount = BigDecimal.ZERO;
            if (p.getPrice() != null && p.getSales() != null) {
                amount = p.getPrice().multiply(BigDecimal.valueOf(p.getSales()));
            }
            return new MerchantProductStatisticsResponse.TopSalesProduct(p.getProductId(), p.getProductName(), p.getImage(), p.getSales(), amount, p.getStock());
        }).toList();

        List<Product> lowStock = productMapper.selectList(new LambdaQueryWrapper<Product>()
                .eq(Product::getShopId, shopId)
                .lt(Product::getStock, 10)
                .orderByAsc(Product::getStock)
                .last("limit " + limit));

        List<MerchantProductStatisticsResponse.LowStockProduct> low = lowStock.stream().map(p -> new MerchantProductStatisticsResponse.LowStockProduct(
                p.getProductId(), p.getProductName(), p.getImage(), p.getStock(), p.getSales()
        )).toList();

        return new MerchantProductStatisticsResponse(topSales, low);
    }

    @Override
    public MerchantCustomerStatisticsResponse customer(long shopId) {
        List<Order> orders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getShopId, shopId)
                .orderByDesc(Order::getCreateTime));

        Map<Long, List<Order>> byUser = new HashMap<>();
        for (Order o : orders) {
            if (o.getUserId() == null) {
                continue;
            }
            byUser.computeIfAbsent(o.getUserId(), k -> new ArrayList<>()).add(o);
        }

        long totalCustomerCount = byUser.size();
        long newCustomerCount = 0;
        long activeCustomerCount = 0;

        List<Long> userIds = new ArrayList<>(byUser.keySet());
        Map<Long, UserAccount> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            for (UserAccount u : userAccountMapper.selectBatchIds(userIds)) {
                userMap.put(u.getUserId(), u);
            }
        }

        List<MerchantCustomerStatisticsResponse.TopCustomer> topCustomers = byUser.entrySet().stream().map(e -> {
            Long userId = e.getKey();
            List<Order> os = e.getValue();
            long orderCount = os.size();
            BigDecimal totalAmount = os.stream().map(o -> o.getActualAmount() == null ? BigDecimal.ZERO : o.getActualAmount()).reduce(BigDecimal.ZERO, BigDecimal::add);
            LocalDateTime last = os.stream().map(Order::getCreateTime).filter(Objects::nonNull).max(LocalDateTime::compareTo).orElse(null);

            UserAccount u = userMap.get(userId);
            String username = u == null ? "用户" : maskName(u.getNickname() != null ? u.getNickname() : u.getUsername());
            return new MerchantCustomerStatisticsResponse.TopCustomer(userId, username, orderCount, totalAmount, last == null ? null : last.format(DATETIME));
        }).sorted((a, b) -> b.getTotalAmount().compareTo(a.getTotalAmount()))
                .limit(10)
                .toList();

        return new MerchantCustomerStatisticsResponse(totalCustomerCount, newCustomerCount, activeCustomerCount, topCustomers);
    }

    private List<Order> listOrders(long shopId, DateRange range) {
        return orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getShopId, shopId)
                .ge(Order::getCreateTime, range.start)
                .le(Order::getCreateTime, range.end));
    }

    private boolean isPaid(Order o) {
        return o.getStatus() != null && o.getStatus() >= 2 && o.getStatus() != 6;
    }

    private long countPaid(List<Order> orders) {
        return orders.stream().filter(this::isPaid).count();
    }

    private BigDecimal sumTotal(List<Order> orders) {
        BigDecimal total = BigDecimal.ZERO;
        for (Order o : orders) {
            if (o.getTotalAmount() != null) {
                total = total.add(o.getTotalAmount());
            }
        }
        return total;
    }

    private BigDecimal sumPaid(List<Order> orders) {
        BigDecimal total = BigDecimal.ZERO;
        for (Order o : orders) {
            if (o.getActualAmount() != null) {
                total = total.add(o.getActualAmount());
            }
        }
        return total;
    }

    private long countRefunds(long shopId, DateRange range) {
        return refundMapper.selectCount(new LambdaQueryWrapper<Refund>()
                .eq(Refund::getShopId, shopId)
                .ge(Refund::getCreateTime, range.start)
                .le(Refund::getCreateTime, range.end));
    }

    private double rate(int today, int yesterday) {
        if (yesterday == 0) {
            return today == 0 ? 0D : 100D;
        }
        return (today - yesterday) * 100.0 / yesterday;
    }

    private double rate(BigDecimal today, BigDecimal yesterday) {
        if (yesterday == null || yesterday.compareTo(BigDecimal.ZERO) == 0) {
            return today == null || today.compareTo(BigDecimal.ZERO) == 0 ? 0D : 100D;
        }
        return today.subtract(yesterday).multiply(BigDecimal.valueOf(100)).divide(yesterday, 2, RoundingMode.HALF_UP).doubleValue();
    }

    private List<MerchantSalesStatisticsResponse.TrendData> buildSalesTrend(DateRange range, List<Order> orders) {
        Map<String, List<Order>> byDate = new HashMap<>();
        for (Order o : orders) {
            if (o.getCreateTime() == null) {
                continue;
            }
            String k = o.getCreateTime().toLocalDate().format(DATE);
            byDate.computeIfAbsent(k, x -> new ArrayList<>()).add(o);
        }

        List<MerchantSalesStatisticsResponse.TrendData> list = new ArrayList<>();
        LocalDate cur = range.start.toLocalDate();
        LocalDate end = range.end.toLocalDate();
        while (!cur.isAfter(end)) {
            String key = cur.format(DATE);
            List<Order> os = byDate.getOrDefault(key, List.of());
            long oc = os.size();
            BigDecimal oa = sumTotal(os);
            List<Order> paid = os.stream().filter(this::isPaid).toList();
            long pc = paid.size();
            BigDecimal pa = sumPaid(paid);
            list.add(new MerchantSalesStatisticsResponse.TrendData(key, oc, oa, pc, pa));
            cur = cur.plusDays(1);
        }
        return list;
    }

    private List<MerchantSalesStatisticsResponse.CategoryData> buildCategoryData(long shopId, DateRange range) {
        List<Order> orders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getShopId, shopId)
                .ge(Order::getCreateTime, range.start)
                .le(Order::getCreateTime, range.end));

        if (orders.isEmpty()) {
            return List.of();
        }

        Set<Long> orderIds = new HashSet<>();
        for (Order o : orders) {
            orderIds.add(o.getOrderId());
        }

        List<OrderItem> items = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>().in(OrderItem::getOrderId, orderIds));
        if (items.isEmpty()) {
            return List.of();
        }

        Set<Long> productIds = new HashSet<>();
        for (OrderItem oi : items) {
            productIds.add(oi.getProductId());
        }

        Map<Long, Product> productMap = new HashMap<>();
        for (Product p : productMapper.selectBatchIds(productIds)) {
            productMap.put(p.getProductId(), p);
        }

        Set<Long> categoryIds = new HashSet<>();
        for (Product p : productMap.values()) {
            if (p.getCategoryId() != null) {
                categoryIds.add(p.getCategoryId());
            }
        }

        Map<Long, ProductCategory> categoryMap = new HashMap<>();
        for (ProductCategory c : productCategoryMapper.selectBatchIds(categoryIds)) {
            categoryMap.put(c.getCategoryId(), c);
        }

        Map<String, Stat> statMap = new HashMap<>();
        BigDecimal grandTotal = BigDecimal.ZERO;

        for (OrderItem oi : items) {
            Product p = productMap.get(oi.getProductId());
            if (p == null) {
                continue;
            }
            ProductCategory c = categoryMap.get(p.getCategoryId());
            String name = c == null ? "未分类" : c.getCategoryName();

            BigDecimal subtotal = oi.getSubtotal() == null ? BigDecimal.ZERO : oi.getSubtotal();
            grandTotal = grandTotal.add(subtotal);

            Stat st = statMap.computeIfAbsent(name, k -> new Stat());
            st.count++;
            st.amount = st.amount.add(subtotal);
        }

        List<MerchantSalesStatisticsResponse.CategoryData> list = new ArrayList<>();
        for (Map.Entry<String, Stat> e : statMap.entrySet()) {
            double pct = grandTotal.compareTo(BigDecimal.ZERO) == 0 ? 0D : e.getValue().amount.multiply(BigDecimal.valueOf(100)).divide(grandTotal, 2, RoundingMode.HALF_UP).doubleValue();
            list.add(new MerchantSalesStatisticsResponse.CategoryData(e.getKey(), e.getValue().count, e.getValue().amount, pct));
        }

        list.sort((a, b) -> b.getOrderAmount().compareTo(a.getOrderAmount()));
        return list;
    }

    private DateRange dateRange(String dateType, String startDate, String endDate) {
        if (dateType == null || dateType.isBlank()) {
            dateType = "today";
        }

        LocalDate today = LocalDate.now();

        if ("custom".equalsIgnoreCase(dateType) && startDate != null && endDate != null) {
            LocalDate s = LocalDate.parse(startDate, DATE);
            LocalDate e = LocalDate.parse(endDate, DATE);
            return new DateRange(s.atStartOfDay(), LocalDateTime.of(e, LocalTime.MAX));
        }

        LocalDate start;
        switch (dateType) {
            case "week" -> start = today.minusDays(6);
            case "month" -> start = today.minusDays(29);
            case "year" -> start = today.minusDays(364);
            default -> start = today;
        }

        return new DateRange(start.atStartOfDay(), LocalDateTime.of(today, LocalTime.MAX));
    }

    private record DateRange(LocalDateTime start, LocalDateTime end) {
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

    private static class Stat {
        long count = 0;
        BigDecimal amount = BigDecimal.ZERO;
    }
}
