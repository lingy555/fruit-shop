package com.lingnan.fruitshop.service.impl;

import com.lingnan.fruitshop.service.AdminDashboardService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lingnan.fruitshop.dto.admin.dashboard.vo.AdminChartDataItemResponse;
import com.lingnan.fruitshop.dto.admin.dashboard.vo.AdminPlatformDataResponse;
import com.lingnan.fruitshop.dto.admin.dashboard.vo.AdminTodayDataResponse;
import com.lingnan.fruitshop.entity.*;
import com.lingnan.fruitshop.mapper.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final OrderMapper orderMapper;
    private final UserAccountMapper userAccountMapper;
    private final MerchantAccountMapper merchantAccountMapper;
    private final ProductMapper productMapper;
    private final ProductCategoryMapper productCategoryMapper;

    public AdminDashboardServiceImpl(OrderMapper orderMapper,
                                UserAccountMapper userAccountMapper,
                                MerchantAccountMapper merchantAccountMapper,
                                ProductMapper productMapper,
                                ProductCategoryMapper productCategoryMapper) {
        this.orderMapper = orderMapper;
        this.userAccountMapper = userAccountMapper;
        this.merchantAccountMapper = merchantAccountMapper;
        this.productMapper = productMapper;
        this.productCategoryMapper = productCategoryMapper;
    }

    @Override
    public AdminTodayDataResponse todayData(String date) {
        LocalDate d = date == null || date.isBlank() ? LocalDate.now() : LocalDate.parse(date, DATE);
        LocalDate yesterday = d.minusDays(1);

        TodayAgg today = orderAgg(d);
        TodayAgg yday = orderAgg(yesterday);

        long visitCount = 0;
        long yVisitCount = 0;

        long newUserCount = safeLong(userAccountMapper.selectCount(new LambdaQueryWrapper<UserAccount>()
                .ge(UserAccount::getCreateTime, d.atStartOfDay())
                .lt(UserAccount::getCreateTime, d.plusDays(1).atStartOfDay())));

        long yNewUserCount = safeLong(userAccountMapper.selectCount(new LambdaQueryWrapper<UserAccount>()
                .ge(UserAccount::getCreateTime, yesterday.atStartOfDay())
                .lt(UserAccount::getCreateTime, yesterday.plusDays(1).atStartOfDay())));

        return new AdminTodayDataResponse(
                today.orderCount,
                compareRate(today.orderCount, yday.orderCount),
                today.orderAmount,
                compareRate(today.orderAmount, yday.orderAmount),
                visitCount,
                compareRate(visitCount, yVisitCount),
                newUserCount,
                compareRate(newUserCount, yNewUserCount)
        );
    }

    @Override
    public AdminPlatformDataResponse platformData() {
        long totalUserCount = safeLong(userAccountMapper.selectCount(new LambdaQueryWrapper<UserAccount>()));
        long totalMerchantCount = safeLong(merchantAccountMapper.selectCount(new LambdaQueryWrapper<MerchantAccount>()));
        long totalProductCount = safeLong(productMapper.selectCount(new LambdaQueryWrapper<Product>()));
        long totalOrderCount = safeLong(orderMapper.selectCount(new LambdaQueryWrapper<Order>()));

        BigDecimal totalOrderAmount = BigDecimal.ZERO;
        for (Order o : orderMapper.selectList(new LambdaQueryWrapper<Order>())) {
            if (o.getActualAmount() != null) {
                totalOrderAmount = totalOrderAmount.add(o.getActualAmount());
            }
        }

        return new AdminPlatformDataResponse(totalUserCount, totalMerchantCount, totalProductCount, totalOrderCount, totalOrderAmount);
    }

    @Override
    public List<AdminChartDataItemResponse> chartData(String type, String startDate, String endDate) {
        String t = type == null ? "order" : type;

        if ("product".equalsIgnoreCase(t)) {
            return productChart();
        }

        LocalDate start = startDate == null || startDate.isBlank() ? LocalDate.now().minusDays(6) : LocalDate.parse(startDate, DATE);
        LocalDate end = endDate == null || endDate.isBlank() ? LocalDate.now() : LocalDate.parse(endDate, DATE);
        if (end.isBefore(start)) {
            LocalDate tmp = start;
            start = end;
            end = tmp;
        }

        if ("order".equalsIgnoreCase(t)) {
            return orderChart(start, end);
        }
        if ("user".equalsIgnoreCase(t)) {
            return userChart(start, end);
        }
        if ("merchant".equalsIgnoreCase(t)) {
            return merchantChart(start, end);
        }

        return List.of();
    }

    private List<AdminChartDataItemResponse> orderChart(LocalDate start, LocalDate end) {
        Map<String, TodayAgg> map = new HashMap<>();

        List<Order> orders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .ge(Order::getCreateTime, start.atStartOfDay())
                .lt(Order::getCreateTime, end.plusDays(1).atStartOfDay()));

        for (Order o : orders) {
            if (o.getCreateTime() == null) {
                continue;
            }
            String day = o.getCreateTime().toLocalDate().format(DATE);
            TodayAgg a = map.computeIfAbsent(day, k -> new TodayAgg(0, BigDecimal.ZERO));
            a.orderCount += 1;
            if (o.getActualAmount() != null) {
                a.orderAmount = a.orderAmount.add(o.getActualAmount());
            }
        }

        List<AdminChartDataItemResponse> res = new ArrayList<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            String day = d.format(DATE);
            TodayAgg a = map.getOrDefault(day, new TodayAgg(0, BigDecimal.ZERO));
            res.add(new AdminChartDataItemResponse(day, a.orderCount, a.orderAmount, null, null, null, null, null, null, null));
        }
        return res;
    }

    private List<AdminChartDataItemResponse> userChart(LocalDate start, LocalDate end) {
        Map<String, Long> newCountMap = new HashMap<>();

        List<UserAccount> users = userAccountMapper.selectList(new LambdaQueryWrapper<UserAccount>()
                .ge(UserAccount::getCreateTime, start.atStartOfDay())
                .lt(UserAccount::getCreateTime, end.plusDays(1).atStartOfDay()));

        for (UserAccount u : users) {
            if (u.getCreateTime() == null) {
                continue;
            }
            String day = u.getCreateTime().toLocalDate().format(DATE);
            newCountMap.put(day, newCountMap.getOrDefault(day, 0L) + 1);
        }

        long base = safeLong(userAccountMapper.selectCount(new LambdaQueryWrapper<UserAccount>()
                .lt(UserAccount::getCreateTime, start.atStartOfDay())));

        List<AdminChartDataItemResponse> res = new ArrayList<>();
        long total = base;
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            String day = d.format(DATE);
            long inc = newCountMap.getOrDefault(day, 0L);
            total += inc;
            res.add(new AdminChartDataItemResponse(day, null, null, inc, total, null, null, null, null, null));
        }
        return res;
    }

    private List<AdminChartDataItemResponse> merchantChart(LocalDate start, LocalDate end) {
        Map<String, Long> newCountMap = new HashMap<>();

        List<MerchantAccount> merchants = merchantAccountMapper.selectList(new LambdaQueryWrapper<MerchantAccount>()
                .ge(MerchantAccount::getCreateTime, start.atStartOfDay())
                .lt(MerchantAccount::getCreateTime, end.plusDays(1).atStartOfDay()));

        for (MerchantAccount m : merchants) {
            if (m.getCreateTime() == null) {
                continue;
            }
            String day = m.getCreateTime().toLocalDate().format(DATE);
            newCountMap.put(day, newCountMap.getOrDefault(day, 0L) + 1);
        }

        long base = safeLong(merchantAccountMapper.selectCount(new LambdaQueryWrapper<MerchantAccount>()
                .lt(MerchantAccount::getCreateTime, start.atStartOfDay())));

        List<AdminChartDataItemResponse> res = new ArrayList<>();
        long total = base;
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            String day = d.format(DATE);
            long inc = newCountMap.getOrDefault(day, 0L);
            total += inc;
            res.add(new AdminChartDataItemResponse(day, null, null, null, null, inc, total, null, null, null));
        }
        return res;
    }

    private List<AdminChartDataItemResponse> productChart() {
        List<ProductCategory> categories = productCategoryMapper.selectList(new LambdaQueryWrapper<ProductCategory>()
                .orderByAsc(ProductCategory::getSort));

        Map<Long, String> categoryNameMap = new HashMap<>();
        for (ProductCategory c : categories) {
            categoryNameMap.put(c.getCategoryId(), c.getCategoryName());
        }

        Map<Long, Long> countMap = new HashMap<>();
        Map<Long, Long> salesMap = new HashMap<>();
        for (Product p : productMapper.selectList(new LambdaQueryWrapper<Product>())) {
            if (p.getCategoryId() == null) {
                continue;
            }
            countMap.put(p.getCategoryId(), countMap.getOrDefault(p.getCategoryId(), 0L) + 1);
            long sales = p.getSales() == null ? 0 : p.getSales();
            salesMap.put(p.getCategoryId(), salesMap.getOrDefault(p.getCategoryId(), 0L) + sales);
        }

        List<AdminChartDataItemResponse> res = new ArrayList<>();
        for (Map.Entry<Long, Long> e : countMap.entrySet()) {
            Long cid = e.getKey();
            res.add(new AdminChartDataItemResponse(
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    categoryNameMap.getOrDefault(cid, String.valueOf(cid)),
                    e.getValue(),
                    salesMap.getOrDefault(cid, 0L)
            ));
        }
        res.sort(Comparator.comparing(AdminChartDataItemResponse::getProductCount, Comparator.nullsLast(Comparator.reverseOrder())));
        return res;
    }

    private TodayAgg orderAgg(LocalDate d) {
        LocalDateTime start = d.atStartOfDay();
        LocalDateTime end = d.plusDays(1).atStartOfDay();

        long count = safeLong(orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                .ge(Order::getCreateTime, start)
                .lt(Order::getCreateTime, end)));

        BigDecimal amount = BigDecimal.ZERO;
        for (Order o : orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .ge(Order::getCreateTime, start)
                .lt(Order::getCreateTime, end))) {
            if (o.getActualAmount() != null) {
                amount = amount.add(o.getActualAmount());
            }
        }

        return new TodayAgg(count, amount);
    }

    private long safeLong(Long v) {
        return v == null ? 0 : v;
    }

    private Double compareRate(long today, long yesterday) {
        if (yesterday == 0) {
            return today == 0 ? null : 100D;
        }
        return round2((today - yesterday) * 100.0 / yesterday);
    }

    private Double compareRate(BigDecimal today, BigDecimal yesterday) {
        BigDecimal y = yesterday == null ? BigDecimal.ZERO : yesterday;
        BigDecimal t = today == null ? BigDecimal.ZERO : today;
        if (y.compareTo(BigDecimal.ZERO) == 0) {
            return t.compareTo(BigDecimal.ZERO) == 0 ? null : 100D;
        }
        BigDecimal diff = t.subtract(y);
        BigDecimal rate = diff.multiply(BigDecimal.valueOf(100)).divide(y, 4, RoundingMode.HALF_UP);
        return round2(rate.doubleValue());
    }

    private double round2(double v) {
        return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private static class TodayAgg {
        long orderCount;
        BigDecimal orderAmount;

        TodayAgg(long orderCount, BigDecimal orderAmount) {
            this.orderCount = orderCount;
            this.orderAmount = orderAmount;
        }
    }
}
