-- 为order表添加支付宝支付相关字段
-- 执行时间: 2026-01-08

ALTER TABLE `order` 
ADD COLUMN `pay_method` VARCHAR(20) DEFAULT NULL COMMENT '支付方式：WECHAT,ALIPAY,BALANCE' AFTER `payment_method`,
ADD COLUMN `trade_no` VARCHAR(100) DEFAULT NULL COMMENT '第三方支付交易号' AFTER `pay_method`;
