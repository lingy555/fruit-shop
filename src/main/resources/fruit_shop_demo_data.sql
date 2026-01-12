SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE fruit_shop;

-- 说明：fruit_shop.sql 末尾已自带一小段初始化数据（管理员/用户1001/店铺10/商品1/SKU1-2/轮播图/公告）。
-- 本文件用于补充更多业务数据，便于前后端联调。

-- 更多用户
INSERT INTO user_account (user_id, username, password_hash, phone, email, nickname, level, points, balance, status)
VALUES
  (1002, 'user002', '123456', '13800138002', 'user002@example.com', '小红', 1, 120, 88.80, 1),
  (1003, 'user003', '123456', '13800138003', 'user003@example.com', '小刚', 1, 0, 0.00, 1);

INSERT INTO user_address (address_id, user_id, receiver_name, receiver_phone, province, city, district, detail, is_default)
VALUES
  (2, 1002, '李四', '13800138002', '广东省', '广州市', '天河区', '体育西路B座', 1),
  (3, 1003, '王五', '13800138003', '广东省', '佛山市', '禅城区', '季华路C座', 1);

-- 优惠券与领券
INSERT INTO coupon (coupon_id, coupon_name, coupon_type, discount_type, discount_value, min_amount, total_count, received_count, used_count, limit_per_user, valid_days, start_time, end_time, status, description)
VALUES
  (1, '新人立减券', 1, 1, 10.00, 0.00, 10000, 1, 0, 1, 30, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), 1, '新用户专享立减10元'),
  (2, '满199减20', 2, 1, 20.00, 199.00, 5000, 1, 0, 2, 60, NOW(), DATE_ADD(NOW(), INTERVAL 60 DAY), 1, '满199元可用');

INSERT INTO user_coupon (user_coupon_id, user_id, coupon_id, status, receive_time, use_time, order_id)
VALUES
  (1, 1001, 1, 0, NOW(), NULL, NULL),
  (2, 1002, 2, 0, NOW(), NULL, NULL);

-- 购物车
INSERT INTO cart_item (cart_id, user_id, shop_id, product_id, sku_id, quantity, selected, is_valid)
VALUES
  (1, 1001, 10, 1, 1, 2, 1, 1),
  (2, 1002, 10, 1, 2, 1, 1, 1);

-- 订单：待发货
INSERT INTO `order` (
  order_id, order_no, user_id, shop_id, status,
  total_amount, shipping_fee, coupon_discount, points_discount, actual_amount,
  payment_method, remark, merchant_remark,
  receiver_name, receiver_phone, province, city, district, address_detail,
  create_time, pay_time, deliver_time, receive_time, finish_time, cancel_time
) VALUES (
  5001, 'O202512290001', 1001, 10, 2,
  179.80, 0.00, 10.00, 0.00, 169.80,
  'ALIPAY', '尽快发货', NULL,
  '张三', '13800138000', '广东省', '深圳市', '南山区', '科技园南区A座',
  NOW(), NOW(), NULL, NULL, NULL, NULL
);

INSERT INTO order_item (
  order_item_id, order_id, product_id, sku_id, product_name, image,
  price, cost_price, specs, quantity, subtotal, can_comment, create_time
) VALUES (
  6001, 5001, 1, 1, '泰国金枕榴莲', 'https://example.com/p1.jpg',
  89.90, 60.00, '重量:3-4斤', 2, 179.80, 0, NOW()
);

INSERT INTO order_logistics (logistics_id, order_id, express_company, express_no, current_status, traces_json)
VALUES (1, 5001, NULL, NULL, '待发货', NULL);

-- 订单：已完成（用于评论/退款演示）
INSERT INTO `order` (
  order_id, order_no, user_id, shop_id, status,
  total_amount, shipping_fee, coupon_discount, points_discount, actual_amount,
  payment_method, remark, merchant_remark,
  receiver_name, receiver_phone, province, city, district, address_detail,
  create_time, pay_time, deliver_time, receive_time, finish_time, cancel_time
) VALUES (
  5002, 'O202512290002', 1002, 10, 5,
  139.90, 0.00, 0.00, 0.00, 139.90,
  'WECHAT', NULL, '客户要求送到门口',
  '李四', '13800138002', '广东省', '广州市', '天河区', '体育西路B座',
  DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), NULL
);

INSERT INTO order_item (
  order_item_id, order_id, product_id, sku_id, product_name, image,
  price, cost_price, specs, quantity, subtotal, can_comment, create_time
) VALUES (
  6002, 5002, 1, 2, '泰国金枕榴莲', 'https://example.com/p1.jpg',
  139.90, 60.00, '重量:5-6斤', 1, 139.90, 1, DATE_SUB(NOW(), INTERVAL 5 DAY)
);

INSERT INTO order_logistics (logistics_id, order_id, express_company, express_no, current_status, traces_json, create_time)
VALUES (
  2, 5002, '顺丰', 'SF1234567890', '已签收',
  JSON_ARRAY(
    JSON_OBJECT('time', DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 4 DAY), '%Y-%m-%d %H:%i:%s'), 'status', '已揽收'),
    JSON_OBJECT('time', DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 3 DAY), '%Y-%m-%d %H:%i:%s'), 'status', '运输中'),
    JSON_OBJECT('time', DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 2 DAY), '%Y-%m-%d %H:%i:%s'), 'status', '派送中'),
    JSON_OBJECT('time', DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 2 DAY), '%Y-%m-%d %H:%i:%s'), 'status', '已签收')
  ),
  DATE_SUB(NOW(), INTERVAL 4 DAY)
);

-- 评论
INSERT INTO comment (
  comment_id, order_id, order_item_id, user_id, product_id, shop_id,
  score, content, images_json, specs, is_anonymous, status,
  merchant_reply_content, merchant_reply_time,
  append_content, append_images_json, append_time,
  create_time
) VALUES (
  7001, 5002, 6002, 1002, 1, 10,
  5, '榴莲很新鲜，味道很香！', JSON_ARRAY('https://example.com/c1.jpg'), '重量:5-6斤', 0, 1,
  '感谢支持，欢迎下次再来～', DATE_SUB(NOW(), INTERVAL 1 DAY),
  NULL, NULL, NULL,
  DATE_SUB(NOW(), INTERVAL 1 DAY)
);

-- 退款（演示：商家拒绝后管理员仲裁支持退款）
INSERT INTO refund (
  refund_id, refund_no, order_id, order_item_id, user_id, shop_id,
  type, reason, description, images_json, refund_amount,
  status, merchant_reply, reject_reason, arbitrate_result, arbitrate_reason,
  create_time, update_time
) VALUES (
  8001, 'R202512290001', 5002, 6002, 1002, 10,
  1, '商品与描述不符', '收到后发现重量偏轻，申请退款。', JSON_ARRAY('https://example.com/r1.jpg'), 50.00,
  3, '不同意退款', '重量属于正常浮动', 1, '平台核实后支持退款',
  DATE_SUB(NOW(), INTERVAL 1 DAY), NOW()
);

-- 店铺流水（可用于后台统计展示）
INSERT INTO shop_finance_bill (bill_id, shop_id, type, amount, description, create_time)
VALUES
  (1, 10, 1, 169.80, '订单O202512290001成交', NOW()),
  (2, 10, 1, 139.90, '订单O202512290002成交', DATE_SUB(NOW(), INTERVAL 1 DAY));

-- 更多轮播图/公告
INSERT INTO banner (banner_id, title, image, position, link_type, link_value, sort, status)
VALUES
  (2, '热卖榴莲', 'https://example.com/banner2.jpg', 'home', 'product', '1', 2, 1);

INSERT INTO notice (notice_id, title, content, type, status, sort)
VALUES
  (2, '新用户福利上线', '注册即送新人券，快来领取！', 1, 1, 2);

SET FOREIGN_KEY_CHECKS = 1;
