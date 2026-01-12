package com.lingnan.fruitshop.service.impl;

import com.lingnan.fruitshop.service.MerchantShopService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.lingnan.fruitshop.common.exception.BizException;
import com.lingnan.fruitshop.dto.merchant.shop.MerchantShopStatusRequest;
import com.lingnan.fruitshop.dto.merchant.shop.MerchantShopUpdateRequest;
import com.lingnan.fruitshop.dto.merchant.shop.vo.MerchantShopInfoResponse;
import com.lingnan.fruitshop.dto.merchant.shop.vo.MerchantShopStatisticsResponse;
import com.lingnan.fruitshop.entity.MerchantAccount;
import com.lingnan.fruitshop.entity.Order;
import com.lingnan.fruitshop.entity.Product;
import com.lingnan.fruitshop.entity.Shop;
import com.lingnan.fruitshop.mapper.MerchantAccountMapper;
import com.lingnan.fruitshop.mapper.OrderMapper;
import com.lingnan.fruitshop.mapper.ProductMapper;
import com.lingnan.fruitshop.mapper.ShopMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class MerchantShopServiceImpl implements MerchantShopService {

    private static final DateTimeFormatter DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final MerchantAccountMapper merchantAccountMapper;
    private final ShopMapper shopMapper;
    private final ProductMapper productMapper;
    private final OrderMapper orderMapper;

    public MerchantShopServiceImpl(MerchantAccountMapper merchantAccountMapper,
                               ShopMapper shopMapper,
                               ProductMapper productMapper,
                               OrderMapper orderMapper) {
        this.merchantAccountMapper = merchantAccountMapper;
        this.shopMapper = shopMapper;
        this.productMapper = productMapper;
        this.orderMapper = orderMapper;
    }

    @Override
    public MerchantShopInfoResponse info(long merchantId, long shopId) {
        MerchantAccount merchant = merchantAccountMapper.selectById(merchantId);
        Shop shop = shopMapper.selectById(shopId);
        if (merchant == null || shop == null) {
            throw BizException.notFound("店铺不存在");
        }

        Long productCount = productMapper.selectCount(new LambdaQueryWrapper<Product>()
                .eq(Product::getShopId, shopId));

        List<String> qualifications = new ArrayList<>();
        if (merchant.getIdCardFront() != null) {
            qualifications.add(merchant.getIdCardFront());
        }
        if (merchant.getIdCardBack() != null) {
            qualifications.add(merchant.getIdCardBack());
        }

        return new MerchantShopInfoResponse(
                shop.getShopId(),
                shop.getShopName(),
                shop.getLogo(),
                shop.getBanner(),
                shop.getDescription(),
                shop.getProvince(),
                shop.getCity(),
                shop.getDistrict(),
                shop.getAddress(),
                shop.getContactName() != null ? shop.getContactName() : merchant.getContactName(),
                shop.getContactPhone() != null ? shop.getContactPhone() : merchant.getContactPhone(),
                shop.getBusinessHours(),
                shop.getStatus(),
                shop.getScore(),
                productCount == null ? 0 : productCount,
                shop.getTotalSalesAmount() == null ? BigDecimal.ZERO : shop.getTotalSalesAmount(),
                shop.getFanCount() == null ? 0 : shop.getFanCount(),
                shop.getCreateTime() == null ? null : shop.getCreateTime().format(DATETIME),
                merchant.getBusinessLicense(),
                qualifications
        );
    }

    @Override
    public void update(long merchantId, long shopId, MerchantShopUpdateRequest req) {
        Shop shop = shopMapper.selectById(shopId);
        if (shop == null) {
            throw BizException.notFound("店铺不存在");
        }

        shopMapper.update(null, new LambdaUpdateWrapper<Shop>()
                .eq(Shop::getShopId, shopId)
                .set(req.getShopName() != null, Shop::getShopName, req.getShopName())
                .set(req.getLogo() != null, Shop::getLogo, req.getLogo())
                .set(req.getBanner() != null, Shop::getBanner, req.getBanner())
                .set(req.getDescription() != null, Shop::getDescription, req.getDescription())
                .set(req.getProvince() != null, Shop::getProvince, req.getProvince())
                .set(req.getCity() != null, Shop::getCity, req.getCity())
                .set(req.getDistrict() != null, Shop::getDistrict, req.getDistrict())
                .set(req.getAddress() != null, Shop::getAddress, req.getAddress())
                .set(req.getContactPhone() != null, Shop::getContactPhone, req.getContactPhone())
                .set(req.getBusinessHours() != null, Shop::getBusinessHours, req.getBusinessHours()));

        if (req.getShopName() != null) {
            merchantAccountMapper.update(null, new LambdaUpdateWrapper<MerchantAccount>()
                    .eq(MerchantAccount::getMerchantId, merchantId)
                    .set(MerchantAccount::getShopName, req.getShopName()));
        }
    }

    @Override
    public void setStatus(long shopId, MerchantShopStatusRequest req) {
        shopMapper.update(null, new LambdaUpdateWrapper<Shop>()
                .eq(Shop::getShopId, shopId)
                .set(Shop::getStatus, req.getStatus()));
    }

    @Override
    public MerchantShopStatisticsResponse statistics(long shopId, String dateType) {
        DateRange range = resolveRange(dateType);

        List<Order> ordersInRange = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getShopId, shopId)
                .ge(Order::getCreateTime, range.start)
                .le(Order::getCreateTime, range.end));

        long orderCount = ordersInRange.size();
        BigDecimal orderAmount = sum(ordersInRange, true);

        List<Order> paid = ordersInRange.stream().filter(o -> o.getStatus() != null && o.getStatus() >= 2 && o.getStatus() != 6).toList();
        long payOrderCount = paid.size();
        BigDecimal payOrderAmount = sum(paid, false);

        MerchantShopStatisticsResponse.TodayData todayData = new MerchantShopStatisticsResponse.TodayData(
                orderCount,
                orderAmount,
                payOrderCount,
                payOrderAmount,
                0,
                0
        );

        long totalOrderCount = orderMapper.selectCount(new LambdaQueryWrapper<Order>().eq(Order::getShopId, shopId));
        BigDecimal totalOrderAmount = sum(orderMapper.selectList(new LambdaQueryWrapper<Order>().eq(Order::getShopId, shopId)), true);
        long totalProductCount = productMapper.selectCount(new LambdaQueryWrapper<Product>().eq(Product::getShopId, shopId));

        Shop shop = shopMapper.selectById(shopId);
        long totalFanCount = shop != null && shop.getFanCount() != null ? shop.getFanCount() : 0;

        MerchantShopStatisticsResponse.TotalData totalData = new MerchantShopStatisticsResponse.TotalData(
                totalOrderCount,
                totalOrderAmount,
                totalProductCount,
                totalFanCount
        );

        List<MerchantShopStatisticsResponse.TrendData> trendData = buildTrend(range, ordersInRange);

        return new MerchantShopStatisticsResponse(todayData, totalData, trendData);
    }

    private List<MerchantShopStatisticsResponse.TrendData> buildTrend(DateRange range, List<Order> orders) {
        Map<String, List<Order>> byDate = new HashMap<>();
        for (Order o : orders) {
            LocalDate d = o.getCreateTime().toLocalDate();
            byDate.computeIfAbsent(d.format(DATE), k -> new ArrayList<>()).add(o);
        }

        List<MerchantShopStatisticsResponse.TrendData> list = new ArrayList<>();
        LocalDate cur = range.start.toLocalDate();
        LocalDate end = range.end.toLocalDate();
        while (!cur.isAfter(end)) {
            String key = cur.format(DATE);
            List<Order> os = byDate.getOrDefault(key, List.of());
            list.add(new MerchantShopStatisticsResponse.TrendData(
                    key,
                    os.size(),
                    sum(os, true),
                    0
            ));
            cur = cur.plusDays(1);
        }
        return list;
    }

    private BigDecimal sum(List<Order> orders, boolean useTotalAmount) {
        BigDecimal total = BigDecimal.ZERO;
        for (Order o : orders) {
            BigDecimal amt = useTotalAmount ? o.getTotalAmount() : o.getActualAmount();
            if (amt != null) {
                total = total.add(amt);
            }
        }
        return total;
    }

    private DateRange resolveRange(String dateType) {
        if (dateType == null || dateType.isBlank()) {
            dateType = "today";
        }

        LocalDate today = LocalDate.now();
        LocalDate startDate;

        switch (dateType) {
            case "week" -> startDate = today.minusDays(6);
            case "month" -> startDate = today.minusDays(29);
            case "year" -> startDate = today.minusDays(364);
            default -> startDate = today;
        }

        return new DateRange(startDate.atStartOfDay(), LocalDateTime.of(today, LocalTime.MAX));
    }

    private record DateRange(LocalDateTime start, LocalDateTime end) {
    }
}
