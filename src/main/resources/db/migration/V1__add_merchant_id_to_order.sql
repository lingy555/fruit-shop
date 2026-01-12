-- 添加merchantId字段到order表
ALTER TABLE `order` ADD COLUMN `merchant_id` BIGINT COMMENT '商家ID';

-- 更新现有订单的merchantId（假设shopId和merchantId的关系是1:1）
-- 这里需要根据实际的商家-店铺关系来设置
-- 临时设置：假设shopId 10对应merchantId 1001
UPDATE `order` SET `merchant_id` = 1001 WHERE `shop_id` = 10;

-- 如果有其他shopId，需要相应设置merchantId
-- UPDATE `order` SET `merchant_id` = ? WHERE `shop_id` = ?;

-- 查看更新结果
SELECT order_id, shop_id, merchant_id FROM `order` LIMIT 10;
