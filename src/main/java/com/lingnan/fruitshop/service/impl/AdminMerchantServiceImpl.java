package com.lingnan.fruitshop.service.impl;

import com.lingnan.fruitshop.service.AdminMerchantService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingnan.fruitshop.common.exception.BizException;
import com.lingnan.fruitshop.dto.admin.merchant.AdminMerchantAuditRequest;
import com.lingnan.fruitshop.dto.admin.merchant.AdminMerchantUpdateStatusRequest;
import com.lingnan.fruitshop.dto.admin.merchant.vo.AdminMerchantDetailResponse;
import com.lingnan.fruitshop.dto.admin.merchant.vo.AdminMerchantListResponse;
import com.lingnan.fruitshop.dto.admin.merchant.vo.AdminMerchantStatisticsResponse;
import com.lingnan.fruitshop.entity.*;
import com.lingnan.fruitshop.mapper.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class AdminMerchantServiceImpl implements AdminMerchantService {

    private static final DateTimeFormatter DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final MerchantAccountMapper merchantAccountMapper;
    private final ShopMapper shopMapper;
    private final ProductMapper productMapper;
    private final OrderMapper orderMapper;
    private final RefundMapper refundMapper;

    public AdminMerchantServiceImpl(MerchantAccountMapper merchantAccountMapper,
                               ShopMapper shopMapper,
                               ProductMapper productMapper,
                               OrderMapper orderMapper,
                               RefundMapper refundMapper) {
        this.merchantAccountMapper = merchantAccountMapper;
        this.shopMapper = shopMapper;
        this.productMapper = productMapper;
        this.orderMapper = orderMapper;
        this.refundMapper = refundMapper;
    }

    @Override
    public AdminMerchantListResponse list(int page, int pageSize, String keyword, Integer status, String province, String city, String startTime, String endTime) {
        LambdaQueryWrapper<MerchantAccount> qw = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            qw.and(w -> w.like(MerchantAccount::getShopName, keyword)
                    .or().like(MerchantAccount::getContactName, keyword)
                    .or().like(MerchantAccount::getContactPhone, keyword));
        }
        if (status != null) {
            qw.eq(MerchantAccount::getStatus, status);
        }
        if (startTime != null && !startTime.isBlank()) {
            qw.ge(MerchantAccount::getCreateTime, startTime);
        }
        if (endTime != null && !endTime.isBlank()) {
            qw.le(MerchantAccount::getCreateTime, endTime);
        }
        qw.orderByDesc(MerchantAccount::getCreateTime);

        Page<MerchantAccount> p = new Page<>(page, pageSize);
        Page<MerchantAccount> result = merchantAccountMapper.selectPage(p, qw);

        AdminMerchantListResponse.StatusCount statusCount = statusCount();
        if (result.getRecords().isEmpty()) {
            return new AdminMerchantListResponse(0, statusCount, List.of());
        }

        Set<Long> shopIds = new HashSet<>();
        Set<Long> merchantIds = new HashSet<>();
        for (MerchantAccount m : result.getRecords()) {
            merchantIds.add(m.getMerchantId());
            if (m.getShopId() != null) {
                shopIds.add(m.getShopId());
            }
        }

        Map<Long, Shop> shopMap = new HashMap<>();
        if (!shopIds.isEmpty()) {
            for (Shop s : shopMapper.selectBatchIds(shopIds)) {
                shopMap.put(s.getShopId(), s);
            }
        }

        Map<Long, Long> productCountMap = new HashMap<>();
        if (!shopIds.isEmpty()) {
            for (Long shopId : shopIds) {
                Long cnt = productMapper.selectCount(new LambdaQueryWrapper<Product>().eq(Product::getShopId, shopId));
                productCountMap.put(shopId, cnt == null ? 0L : cnt);
            }
        }

        List<AdminMerchantListResponse.Item> list = new ArrayList<>();
        for (MerchantAccount m : result.getRecords()) {
            Shop s = m.getShopId() == null ? null : shopMap.get(m.getShopId());
            if (province != null && !province.isBlank()) {
                if (s == null || s.getProvince() == null || !s.getProvince().contains(province)) {
                    continue;
                }
            }
            if (city != null && !city.isBlank()) {
                if (s == null || s.getCity() == null || !s.getCity().contains(city)) {
                    continue;
                }
            }

            Long shopId = m.getShopId();
            long productCount = shopId == null ? 0 : productCountMap.getOrDefault(shopId, 0L);
            list.add(new AdminMerchantListResponse.Item(
                    m.getMerchantId(),
                    shopId,
                    m.getShopName(),
                    s == null ? null : s.getLogo(),
                    m.getContactName(),
                    m.getContactPhone(),
                    m.getEmail(),
                    s == null ? null : s.getProvince(),
                    s == null ? null : s.getCity(),
                    m.getStatus(),
                    merchantStatusText(m.getStatus()),
                    s == null ? null : s.getScore(),
                    productCount,
                    s == null ? BigDecimal.ZERO : (s.getTotalSalesAmount() == null ? BigDecimal.ZERO : s.getTotalSalesAmount()),
                    s == null || s.getFanCount() == null ? 0 : s.getFanCount(),
                    format(m.getCreateTime()),
                    format(m.getAuditTime())
            ));
        }

        return new AdminMerchantListResponse(result.getTotal(), statusCount, list);
    }

    @Override
    public AdminMerchantDetailResponse detail(long merchantId) {
        MerchantAccount m = merchantAccountMapper.selectById(merchantId);
        if (m == null) {
            throw BizException.notFound("商家不存在");
        }
        Shop s = m.getShopId() == null ? null : shopMapper.selectById(m.getShopId());

        long productCount = m.getShopId() == null ? 0 : safeLong(productMapper.selectCount(new LambdaQueryWrapper<Product>().eq(Product::getShopId, m.getShopId())));
        long totalOrderCount = m.getShopId() == null ? 0 : safeLong(orderMapper.selectCount(new LambdaQueryWrapper<Order>().eq(Order::getShopId, m.getShopId())));

        return new AdminMerchantDetailResponse(
                m.getMerchantId(),
                m.getShopId(),
                m.getShopName(),
                s == null ? null : s.getLogo(),
                s == null ? null : s.getBanner(),
                s == null ? null : s.getDescription(),
                m.getContactName(),
                m.getContactPhone(),
                m.getEmail(),
                s == null ? null : s.getProvince(),
                s == null ? null : s.getCity(),
                s == null ? null : s.getDistrict(),
                s == null ? null : s.getAddress(),
                m.getBusinessLicense(),
                m.getIdCardFront(),
                m.getIdCardBack(),
                List.of(),
                m.getStatus(),
                merchantStatusText(m.getStatus()),
                s == null ? null : s.getScore(),
                productCount,
                s == null ? BigDecimal.ZERO : (s.getTotalSalesAmount() == null ? BigDecimal.ZERO : s.getTotalSalesAmount()),
                totalOrderCount,
                s == null || s.getFanCount() == null ? 0 : s.getFanCount(),
                format(m.getCreateTime()),
                format(m.getAuditTime()),
                m.getAuditRemark()
        );
    }

    @Override
    public void audit(AdminMerchantAuditRequest req) {
        MerchantAccount m = merchantAccountMapper.selectById(req.getMerchantId());
        if (m == null) {
            throw BizException.notFound("商家不存在");
        }

        Integer auditStatus = req.getStatus();
        Integer targetStatus;
        if (auditStatus != null && auditStatus == 1) {
            targetStatus = 1;
        } else if (auditStatus != null && auditStatus == 2) {
            targetStatus = 3;
        } else {
            throw BizException.badRequest("status 参数错误");
        }

        m.setStatus(targetStatus);
        m.setAuditRemark(req.getAuditRemark());
        m.setAuditTime(LocalDateTime.now());
        merchantAccountMapper.updateById(m);

        if (targetStatus == 1) {
            if (m.getShopId() == null) {
                Shop shop = new Shop();
                shop.setMerchantId(m.getMerchantId());
                shop.setShopName(m.getShopName());
                shop.setDescription(null);
                shop.setContactName(m.getContactName());
                shop.setContactPhone(m.getContactPhone());
                shop.setBusinessLicense(m.getBusinessLicense());
                shop.setStatus(1);
                shopMapper.insert(shop);

                merchantAccountMapper.update(null, new LambdaUpdateWrapper<MerchantAccount>()
                        .eq(MerchantAccount::getMerchantId, m.getMerchantId())
                        .set(MerchantAccount::getShopId, shop.getShopId()));
            }
        }
    }

    @Override
    public void updateStatus(AdminMerchantUpdateStatusRequest req) {
        MerchantAccount m = merchantAccountMapper.selectById(req.getMerchantId());
        if (m == null) {
            throw BizException.notFound("商家不存在");
        }
        m.setStatus(req.getStatus());
        if (req.getReason() != null && !req.getReason().isBlank()) {
            m.setAuditRemark(req.getReason());
        }
        merchantAccountMapper.updateById(m);

        if (m.getShopId() != null) {
            Integer shopStatus = req.getStatus() != null && req.getStatus() == 2 ? 0 : 1;
            shopMapper.update(null, new LambdaUpdateWrapper<Shop>()
                    .eq(Shop::getShopId, m.getShopId())
                    .set(Shop::getStatus, shopStatus));
        }
    }

    @Override
    public AdminMerchantStatisticsResponse statistics(long merchantId, String dateType) {
        MerchantAccount m = merchantAccountMapper.selectById(merchantId);
        if (m == null || m.getShopId() == null) {
            return new AdminMerchantStatisticsResponse(0, BigDecimal.ZERO, 0, BigDecimal.ZERO);
        }

        LocalDateTime start = null;
        LocalDateTime end = null;
        LocalDate today = LocalDate.now();
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

        LambdaQueryWrapper<Order> oq = new LambdaQueryWrapper<Order>().eq(Order::getShopId, m.getShopId());
        if (start != null) {
            oq.ge(Order::getCreateTime, start);
        }
        if (end != null) {
            oq.lt(Order::getCreateTime, end);
        }
        List<Order> orders = orderMapper.selectList(oq);

        long orderCount = orders.size();
        BigDecimal salesAmount = BigDecimal.ZERO;
        for (Order o : orders) {
            if (o.getActualAmount() != null) {
                salesAmount = salesAmount.add(o.getActualAmount());
            }
        }

        LambdaQueryWrapper<Refund> rq = new LambdaQueryWrapper<Refund>().eq(Refund::getShopId, m.getShopId());
        if (start != null) {
            rq.ge(Refund::getCreateTime, start);
        }
        if (end != null) {
            rq.lt(Refund::getCreateTime, end);
        }
        List<Refund> refunds = refundMapper.selectList(rq);
        long refundCount = refunds.size();
        BigDecimal refundAmount = BigDecimal.ZERO;
        for (Refund r : refunds) {
            if (r.getRefundAmount() != null) {
                refundAmount = refundAmount.add(r.getRefundAmount());
            }
        }

        return new AdminMerchantStatisticsResponse(orderCount, salesAmount, refundCount, refundAmount);
    }

    private AdminMerchantListResponse.StatusCount statusCount() {
        long pending = safeLong(merchantAccountMapper.selectCount(new LambdaQueryWrapper<MerchantAccount>().eq(MerchantAccount::getStatus, 0)));
        long normal = safeLong(merchantAccountMapper.selectCount(new LambdaQueryWrapper<MerchantAccount>().eq(MerchantAccount::getStatus, 1)));
        long disabled = safeLong(merchantAccountMapper.selectCount(new LambdaQueryWrapper<MerchantAccount>().eq(MerchantAccount::getStatus, 2)));
        long rejected = safeLong(merchantAccountMapper.selectCount(new LambdaQueryWrapper<MerchantAccount>().eq(MerchantAccount::getStatus, 3)));
        return new AdminMerchantListResponse.StatusCount(pending, normal, disabled, rejected);
    }

    private String merchantStatusText(Integer status) {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case 0 -> "待审核";
            case 1 -> "正常";
            case 2 -> "禁用";
            case 3 -> "审核失败";
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
