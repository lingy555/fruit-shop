package com.lingnan.fruitshop.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 数据库字段更新工具
 * 启动时会自动检查并添加缺失的pay_method和trade_no字段
 */
@Component
public class DatabaseUpdateRunner implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        try {
            updateOrderTableIfNeeded();
            expandBannerLinkValueIfNeeded();
        } catch (Exception e) {
            System.err.println("❌ 数据库更新失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateOrderTableIfNeeded() {
        // 检查pay_method字段是否存在
        Integer payMethodCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS " +
            "WHERE TABLE_SCHEMA = DATABASE() " +
            "AND TABLE_NAME = 'order' " +
            "AND COLUMN_NAME = 'pay_method'", Integer.class);

        // 检查trade_no字段是否存在
        Integer tradeNoCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS " +
            "WHERE TABLE_SCHEMA = DATABASE() " +
            "AND TABLE_NAME = 'order' " +
            "AND COLUMN_NAME = 'trade_no'", Integer.class);

        if (payMethodCount == 0 || tradeNoCount == 0) {
            System.out.println("🔧 正在添加订单相关数据库字段...");

            if (payMethodCount == 0) {
                jdbcTemplate.execute("ALTER TABLE `order` ADD COLUMN `pay_method` VARCHAR(20) DEFAULT NULL COMMENT '支付方式：WECHAT,ALIPAY,BALANCE' AFTER `payment_method`");
                System.out.println("✅ 已添加pay_method字段");
            }

            if (tradeNoCount == 0) {
                jdbcTemplate.execute("ALTER TABLE `order` ADD COLUMN `trade_no` VARCHAR(100) DEFAULT NULL COMMENT '第三方支付交易号' AFTER `pay_method`");
                System.out.println("✅ 已添加trade_no字段");
            }

            System.out.println("🎉 订单字段更新完成！");
        } else {
            System.out.println("✅ 订单表字段已存在，无需更新");
        }
    }

    private void expandBannerLinkValueIfNeeded() {
        Integer linkValueLength = jdbcTemplate.queryForObject(
            "SELECT CHARACTER_MAXIMUM_LENGTH FROM INFORMATION_SCHEMA.COLUMNS " +
            "WHERE TABLE_SCHEMA = DATABASE() " +
            "AND TABLE_NAME = 'banner' " +
            "AND COLUMN_NAME = 'link_value'", Integer.class);

        if (linkValueLength != null && linkValueLength < 1024) {
            System.out.println("🔧 正在扩展 banner.link_value 字段长度...");
            jdbcTemplate.execute("ALTER TABLE `banner` MODIFY COLUMN `link_value` VARCHAR(1024) NULL");
            System.out.println("✅ 已将 banner.link_value 扩展至 VARCHAR(1024)");
        } else {
            System.out.println("✅ banner.link_value 字段长度已满足需求");
        }
    }
}
