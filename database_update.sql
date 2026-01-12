-- 数据库字段更新脚本
-- 请在MySQL中手动执行以下命令：

USE fruit_shop;

-- 检查字段是否已存在
SELECT COLUMN_NAME 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'fruit_shop' 
  AND TABLE_NAME = 'order' 
  AND COLUMN_NAME IN ('pay_method', 'trade_no');

-- 如果字段不存在，则添加字段
ALTER TABLE `order` 
ADD COLUMN `pay_method` VARCHAR(20) DEFAULT NULL COMMENT '支付方式：WECHAT,ALIPAY,BALANCE' AFTER `payment_method`,
ADD COLUMN `trade_no` VARCHAR(100) DEFAULT NULL COMMENT '第三方支付交易号' AFTER `pay_method`;

-- 验证字段添加成功
DESCRIBE `order`;
