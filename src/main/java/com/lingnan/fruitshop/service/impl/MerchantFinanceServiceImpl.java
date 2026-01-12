package com.lingnan.fruitshop.service.impl;

import com.lingnan.fruitshop.service.MerchantFinanceService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingnan.fruitshop.dto.customer.common.vo.PageResponse;
import com.lingnan.fruitshop.dto.merchant.finance.vo.MerchantFinanceBillItemResponse;
import com.lingnan.fruitshop.dto.merchant.finance.vo.MerchantFinanceOverviewResponse;
import com.lingnan.fruitshop.entity.ShopFinanceBill;
import com.lingnan.fruitshop.mapper.ShopFinanceBillMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class MerchantFinanceServiceImpl implements MerchantFinanceService {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ShopFinanceBillMapper shopFinanceBillMapper;

    public MerchantFinanceServiceImpl(ShopFinanceBillMapper shopFinanceBillMapper) {
        this.shopFinanceBillMapper = shopFinanceBillMapper;
    }

    @Override
    public MerchantFinanceOverviewResponse overview(long shopId) {
        List<ShopFinanceBill> all = shopFinanceBillMapper.selectList(new LambdaQueryWrapper<ShopFinanceBill>()
                .eq(ShopFinanceBill::getShopId, shopId));

        BigDecimal totalIncome = sumByType(all, 1);
        BigDecimal totalWithdraw = sumByType(all, 3);

        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = LocalDateTime.of(today, LocalTime.MAX);

        List<ShopFinanceBill> todayBills = shopFinanceBillMapper.selectList(new LambdaQueryWrapper<ShopFinanceBill>()
                .eq(ShopFinanceBill::getShopId, shopId)
                .ge(ShopFinanceBill::getCreateTime, start)
                .le(ShopFinanceBill::getCreateTime, end));

        BigDecimal todayIncome = sumByType(todayBills, 1);

        BigDecimal balance = totalIncome.subtract(totalWithdraw);
        if (balance.compareTo(BigDecimal.ZERO) < 0) {
            balance = BigDecimal.ZERO;
        }

        return new MerchantFinanceOverviewResponse(
                balance,
                BigDecimal.ZERO,
                totalIncome,
                totalWithdraw,
                todayIncome,
                BigDecimal.ZERO
        );
    }

    @Override
    public PageResponse<MerchantFinanceBillItemResponse> bills(long shopId, int page, int pageSize, Integer type, String startDate, String endDate) {
        LambdaQueryWrapper<ShopFinanceBill> qw = new LambdaQueryWrapper<ShopFinanceBill>()
                .eq(ShopFinanceBill::getShopId, shopId);

        if (type != null) {
            qw.eq(ShopFinanceBill::getType, type);
        }

        if (startDate != null && !startDate.isBlank()) {
            LocalDate s = LocalDate.parse(startDate, DATE);
            qw.ge(ShopFinanceBill::getCreateTime, s.atStartOfDay());
        }

        if (endDate != null && !endDate.isBlank()) {
            LocalDate e = LocalDate.parse(endDate, DATE);
            qw.le(ShopFinanceBill::getCreateTime, LocalDateTime.of(e, LocalTime.MAX));
        }

        qw.orderByDesc(ShopFinanceBill::getCreateTime);

        Page<ShopFinanceBill> p = new Page<>(page, pageSize);
        Page<ShopFinanceBill> result = shopFinanceBillMapper.selectPage(p, qw);

        List<MerchantFinanceBillItemResponse> list = result.getRecords().stream().map(b -> new MerchantFinanceBillItemResponse(
                b.getBillId(),
                b.getType(),
                typeText(b.getType()),
                b.getAmount(),
                b.getDescription(),
                b.getCreateTime() == null ? null : b.getCreateTime().format(DATETIME)
        )).toList();

        return new PageResponse<>(result.getTotal(), list);
    }

    private BigDecimal sumByType(List<ShopFinanceBill> bills, int type) {
        BigDecimal total = BigDecimal.ZERO;
        for (ShopFinanceBill b : bills) {
            if (b.getType() != null && b.getType() == type && b.getAmount() != null) {
                total = total.add(b.getAmount());
            }
        }
        return total;
    }

    private String typeText(Integer type) {
        if (type == null) {
            return "未知";
        }
        return switch (type) {
            case 1 -> "收入";
            case 2 -> "支出";
            case 3 -> "提现";
            case 4 -> "退款";
            default -> "未知";
        };
    }
}
