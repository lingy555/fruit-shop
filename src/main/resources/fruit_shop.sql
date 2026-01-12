SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP DATABASE IF EXISTS fruit_shop;
CREATE DATABASE fruit_shop DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE fruit_shop;

CREATE TABLE sys_role (
  role_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  role_name VARCHAR(64) NOT NULL,
  role_code VARCHAR(64) NOT NULL,
  description VARCHAR(255) NULL,
  status TINYINT NOT NULL DEFAULT 1,
  sort INT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (role_id),
  UNIQUE KEY uk_sys_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sys_permission (
  permission_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  parent_id BIGINT UNSIGNED NOT NULL DEFAULT 0,
  permission_name VARCHAR(64) NOT NULL,
  permission_code VARCHAR(128) NOT NULL,
  type VARCHAR(16) NOT NULL,
  icon VARCHAR(64) NULL,
  sort INT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (permission_id),
  UNIQUE KEY uk_sys_permission_code (permission_code),
  KEY idx_sys_permission_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sys_role_permission (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  role_id BIGINT UNSIGNED NOT NULL,
  permission_id BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_role_permission (role_id, permission_id),
  KEY idx_role_permission_role (role_id),
  KEY idx_role_permission_permission (permission_id),
  CONSTRAINT fk_role_permission_role FOREIGN KEY (role_id) REFERENCES sys_role (role_id),
  CONSTRAINT fk_role_permission_permission FOREIGN KEY (permission_id) REFERENCES sys_permission (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sys_admin (
  admin_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  username VARCHAR(64) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  nickname VARCHAR(64) NULL,
  avatar VARCHAR(512) NULL,
  phone VARCHAR(32) NULL,
  email VARCHAR(128) NULL,
  status TINYINT NOT NULL DEFAULT 1,
  last_login_time DATETIME NULL,
  last_login_ip VARCHAR(64) NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (admin_id),
  UNIQUE KEY uk_sys_admin_username (username),
  KEY idx_sys_admin_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sys_admin_role (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  admin_id BIGINT UNSIGNED NOT NULL,
  role_id BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_admin_role (admin_id, role_id),
  KEY idx_admin_role_admin (admin_id),
  KEY idx_admin_role_role (role_id),
  CONSTRAINT fk_admin_role_admin FOREIGN KEY (admin_id) REFERENCES sys_admin (admin_id),
  CONSTRAINT fk_admin_role_role FOREIGN KEY (role_id) REFERENCES sys_role (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE user_account (
  user_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  username VARCHAR(64) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  phone VARCHAR(32) NOT NULL,
  email VARCHAR(128) NULL,
  avatar VARCHAR(512) NULL,
  nickname VARCHAR(64) NULL,
  gender TINYINT NULL,
  birthday DATE NULL,
  level INT NOT NULL DEFAULT 1,
  points INT NOT NULL DEFAULT 0,
  balance DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  frozen_balance DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  status TINYINT NOT NULL DEFAULT 1,
  last_login_time DATETIME NULL,
  last_login_ip VARCHAR(64) NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id),
  UNIQUE KEY uk_user_username (username),
  UNIQUE KEY uk_user_phone (phone),
  KEY idx_user_status (status),
  KEY idx_user_level (level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE user_address (
  address_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  receiver_name VARCHAR(64) NOT NULL,
  receiver_phone VARCHAR(32) NOT NULL,
  province VARCHAR(64) NOT NULL,
  city VARCHAR(64) NOT NULL,
  district VARCHAR(64) NOT NULL,
  detail VARCHAR(255) NOT NULL,
  is_default TINYINT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (address_id),
  KEY idx_user_address_user (user_id),
  CONSTRAINT fk_user_address_user FOREIGN KEY (user_id) REFERENCES user_account (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE user_points_record (
  record_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  type TINYINT NOT NULL,
  points INT NOT NULL,
  description VARCHAR(255) NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (record_id),
  KEY idx_user_points_user_time (user_id, create_time),
  CONSTRAINT fk_user_points_user FOREIGN KEY (user_id) REFERENCES user_account (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE merchant_application (
  application_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  shop_name VARCHAR(128) NOT NULL,
  contact_name VARCHAR(64) NOT NULL,
  contact_phone VARCHAR(32) NOT NULL,
  email VARCHAR(128) NULL,
  business_license VARCHAR(512) NULL,
  id_card_front VARCHAR(512) NULL,
  id_card_back VARCHAR(512) NULL,
  password_hash VARCHAR(255) NOT NULL,
  status TINYINT NOT NULL DEFAULT 0,
  audit_time DATETIME NULL,
  audit_remark VARCHAR(255) NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (application_id),
  KEY idx_merchant_app_status (status),
  KEY idx_merchant_app_phone (contact_phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE merchant_account (
  merchant_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  application_id BIGINT UNSIGNED NULL,
  shop_id BIGINT UNSIGNED NULL,
  shop_name VARCHAR(128) NOT NULL,
  contact_name VARCHAR(64) NOT NULL,
  contact_phone VARCHAR(32) NOT NULL,
  email VARCHAR(128) NULL,
  business_license VARCHAR(512) NULL,
  id_card_front VARCHAR(512) NULL,
  id_card_back VARCHAR(512) NULL,
  password_hash VARCHAR(255) NOT NULL,
  status TINYINT NOT NULL DEFAULT 0,
  audit_time DATETIME NULL,
  audit_remark VARCHAR(255) NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (merchant_id),
  UNIQUE KEY uk_merchant_phone (contact_phone),
  KEY idx_merchant_status (status),
  KEY idx_merchant_shop_id (shop_id),
  CONSTRAINT fk_merchant_application FOREIGN KEY (application_id) REFERENCES merchant_application (application_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE shop (
  shop_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  merchant_id BIGINT UNSIGNED NOT NULL,
  shop_name VARCHAR(128) NOT NULL,
  logo VARCHAR(512) NULL,
  banner VARCHAR(512) NULL,
  description VARCHAR(255) NULL,
  province VARCHAR(64) NULL,
  city VARCHAR(64) NULL,
  district VARCHAR(64) NULL,
  address VARCHAR(255) NULL,
  contact_name VARCHAR(64) NULL,
  contact_phone VARCHAR(32) NULL,
  business_hours VARCHAR(64) NULL,
  business_license VARCHAR(512) NULL,
  status TINYINT NOT NULL DEFAULT 1,
  score DECIMAL(3,2) NOT NULL DEFAULT 5.00,
  total_sales_amount DECIMAL(14,2) NOT NULL DEFAULT 0.00,
  total_sales_count BIGINT UNSIGNED NOT NULL DEFAULT 0,
  fan_count BIGINT UNSIGNED NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (shop_id),
  UNIQUE KEY uk_shop_merchant (merchant_id),
  KEY idx_shop_status (status),
  CONSTRAINT fk_shop_merchant FOREIGN KEY (merchant_id) REFERENCES merchant_account (merchant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE merchant_account
  ADD CONSTRAINT fk_merchant_shop FOREIGN KEY (shop_id) REFERENCES shop (shop_id);

CREATE TABLE shop_favorite (
  favorite_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  shop_id BIGINT UNSIGNED NOT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (favorite_id),
  UNIQUE KEY uk_shop_favorite (user_id, shop_id),
  KEY idx_shop_favorite_shop (shop_id),
  CONSTRAINT fk_shop_favorite_user FOREIGN KEY (user_id) REFERENCES user_account (user_id),
  CONSTRAINT fk_shop_favorite_shop FOREIGN KEY (shop_id) REFERENCES shop (shop_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE product_category (
  category_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  category_name VARCHAR(64) NOT NULL,
  icon VARCHAR(512) NULL,
  parent_id BIGINT UNSIGNED NOT NULL DEFAULT 0,
  level INT NOT NULL DEFAULT 1,
  sort INT NOT NULL DEFAULT 0,
  status TINYINT NOT NULL DEFAULT 1,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (category_id),
  KEY idx_category_parent (parent_id),
  KEY idx_category_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE product (
  product_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  shop_id BIGINT UNSIGNED NOT NULL,
  product_name VARCHAR(128) NOT NULL,
  category_id BIGINT UNSIGNED NOT NULL,
  image VARCHAR(512) NULL,
  video_url VARCHAR(512) NULL,
  price DECIMAL(12,2) NOT NULL,
  original_price DECIMAL(12,2) NULL,
  cost_price DECIMAL(12,2) NULL,
  stock INT NOT NULL DEFAULT 0,
  sales BIGINT UNSIGNED NOT NULL DEFAULT 0,
  unit VARCHAR(16) NULL,
  weight VARCHAR(32) NULL,
  description VARCHAR(255) NULL,
  detail MEDIUMTEXT NULL,
  tags_json JSON NULL,
  free_shipping TINYINT NOT NULL DEFAULT 1,
  shipping_fee DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  score DECIMAL(3,2) NOT NULL DEFAULT 5.00,
  comment_count BIGINT UNSIGNED NOT NULL DEFAULT 0,
  view_count BIGINT UNSIGNED NOT NULL DEFAULT 0,
  status TINYINT NOT NULL DEFAULT 2,
  audit_remark VARCHAR(255) NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (product_id),
  KEY idx_product_shop (shop_id),
  KEY idx_product_category (category_id),
  KEY idx_product_status (status),
  KEY idx_product_price (price),
  CONSTRAINT fk_product_shop FOREIGN KEY (shop_id) REFERENCES shop (shop_id),
  CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES product_category (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE product_spec (
  spec_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  product_id BIGINT UNSIGNED NOT NULL,
  spec_name VARCHAR(64) NOT NULL,
  options_json JSON NOT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (spec_id),
  KEY idx_product_spec_product (product_id),
  CONSTRAINT fk_product_spec_product FOREIGN KEY (product_id) REFERENCES product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE product_sku (
  sku_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  product_id BIGINT UNSIGNED NOT NULL,
  sku_code VARCHAR(64) NULL,
  price DECIMAL(12,2) NOT NULL,
  stock INT NOT NULL DEFAULT 0,
  image VARCHAR(512) NULL,
  specs_json JSON NULL,
  status TINYINT NOT NULL DEFAULT 1,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (sku_id),
  KEY idx_sku_product (product_id),
  KEY idx_sku_status (status),
  CONSTRAINT fk_sku_product FOREIGN KEY (product_id) REFERENCES product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE product_image (
  image_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  product_id BIGINT UNSIGNED NOT NULL,
  image_url VARCHAR(512) NOT NULL,
  sort INT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (image_id),
  KEY idx_product_image_product_sort (product_id, sort),
  CONSTRAINT fk_product_image_product FOREIGN KEY (product_id) REFERENCES product (product_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE product_search_history (
  history_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  keyword VARCHAR(128) NOT NULL,
  last_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (history_id),
  UNIQUE KEY uk_search_history (user_id, keyword),
  KEY idx_search_history_user_time (user_id, last_time),
  CONSTRAINT fk_search_history_user FOREIGN KEY (user_id) REFERENCES user_account (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE product_hot_keyword (
  keyword_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  keyword VARCHAR(128) NOT NULL,
  sort INT NOT NULL DEFAULT 0,
  status TINYINT NOT NULL DEFAULT 1,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (keyword_id),
  UNIQUE KEY uk_hot_keyword (keyword)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE cart_item (
  cart_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  shop_id BIGINT UNSIGNED NOT NULL,
  product_id BIGINT UNSIGNED NOT NULL,
  sku_id BIGINT UNSIGNED NOT NULL,
  quantity INT NOT NULL,
  selected TINYINT NOT NULL DEFAULT 1,
  is_valid TINYINT NOT NULL DEFAULT 1,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (cart_id),
  UNIQUE KEY uk_cart_user_sku (user_id, sku_id),
  KEY idx_cart_user (user_id),
  KEY idx_cart_shop (shop_id),
  CONSTRAINT fk_cart_user FOREIGN KEY (user_id) REFERENCES user_account (user_id),
  CONSTRAINT fk_cart_shop FOREIGN KEY (shop_id) REFERENCES shop (shop_id),
  CONSTRAINT fk_cart_product FOREIGN KEY (product_id) REFERENCES product (product_id),
  CONSTRAINT fk_cart_sku FOREIGN KEY (sku_id) REFERENCES product_sku (sku_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE coupon (
  coupon_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  coupon_name VARCHAR(128) NOT NULL,
  coupon_type TINYINT NOT NULL,
  discount_type TINYINT NOT NULL,
  discount_value DECIMAL(12,2) NOT NULL,
  min_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  total_count INT NOT NULL DEFAULT 0,
  received_count INT NOT NULL DEFAULT 0,
  used_count INT NOT NULL DEFAULT 0,
  limit_per_user INT NOT NULL DEFAULT 1,
  valid_days INT NULL,
  start_time DATETIME NULL,
  end_time DATETIME NULL,
  status TINYINT NOT NULL DEFAULT 1,
  description VARCHAR(255) NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (coupon_id),
  KEY idx_coupon_status (status),
  KEY idx_coupon_time (start_time, end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE user_coupon (
  user_coupon_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  coupon_id BIGINT UNSIGNED NOT NULL,
  status TINYINT NOT NULL DEFAULT 0,
  receive_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  use_time DATETIME NULL,
  order_id BIGINT UNSIGNED NULL,
  PRIMARY KEY (user_coupon_id),
  UNIQUE KEY uk_user_coupon (user_id, coupon_id, receive_time),
  KEY idx_user_coupon_user_status (user_id, status),
  CONSTRAINT fk_user_coupon_user FOREIGN KEY (user_id) REFERENCES user_account (user_id),
  CONSTRAINT fk_user_coupon_coupon FOREIGN KEY (coupon_id) REFERENCES coupon (coupon_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `order` (
  order_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  order_no VARCHAR(64) NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  shop_id BIGINT UNSIGNED NOT NULL,
  status TINYINT NOT NULL,
  total_amount DECIMAL(12,2) NOT NULL,
  shipping_fee DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  coupon_discount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  points_discount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  actual_amount DECIMAL(12,2) NOT NULL,
  payment_method VARCHAR(32) NULL,
  remark VARCHAR(255) NULL,
  merchant_remark VARCHAR(255) NULL,
  receiver_name VARCHAR(64) NOT NULL,
  receiver_phone VARCHAR(32) NOT NULL,
  province VARCHAR(64) NOT NULL,
  city VARCHAR(64) NOT NULL,
  district VARCHAR(64) NOT NULL,
  address_detail VARCHAR(255) NOT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  pay_time DATETIME NULL,
  deliver_time DATETIME NULL,
  receive_time DATETIME NULL,
  finish_time DATETIME NULL,
  cancel_time DATETIME NULL,
  PRIMARY KEY (order_id),
  UNIQUE KEY uk_order_no (order_no),
  KEY idx_order_user_status (user_id, status),
  KEY idx_order_shop_status (shop_id, status),
  KEY idx_order_create_time (create_time),
  CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES user_account (user_id),
  CONSTRAINT fk_order_shop FOREIGN KEY (shop_id) REFERENCES shop (shop_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE order_item (
  order_item_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  order_id BIGINT UNSIGNED NOT NULL,
  product_id BIGINT UNSIGNED NOT NULL,
  sku_id BIGINT UNSIGNED NOT NULL,
  product_name VARCHAR(128) NOT NULL,
  image VARCHAR(512) NULL,
  price DECIMAL(12,2) NOT NULL,
  cost_price DECIMAL(12,2) NULL,
  specs VARCHAR(255) NULL,
  quantity INT NOT NULL,
  subtotal DECIMAL(12,2) NOT NULL,
  can_comment TINYINT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (order_item_id),
  KEY idx_order_item_order (order_id),
  KEY idx_order_item_product (product_id),
  KEY idx_order_item_sku (sku_id),
  CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES `order` (order_id),
  CONSTRAINT fk_order_item_product FOREIGN KEY (product_id) REFERENCES product (product_id),
  CONSTRAINT fk_order_item_sku FOREIGN KEY (sku_id) REFERENCES product_sku (sku_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE order_logistics (
  logistics_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  order_id BIGINT UNSIGNED NOT NULL,
  express_company VARCHAR(64) NULL,
  express_no VARCHAR(64) NULL,
  current_status VARCHAR(64) NULL,
  traces_json JSON NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (logistics_id),
  UNIQUE KEY uk_order_logistics_order (order_id),
  CONSTRAINT fk_order_logistics_order FOREIGN KEY (order_id) REFERENCES `order` (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE refund (
  refund_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  refund_no VARCHAR(64) NOT NULL,
  order_id BIGINT UNSIGNED NOT NULL,
  order_item_id BIGINT UNSIGNED NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  shop_id BIGINT UNSIGNED NOT NULL,
  type TINYINT NOT NULL,
  reason VARCHAR(128) NOT NULL,
  description VARCHAR(255) NULL,
  images_json JSON NULL,
  refund_amount DECIMAL(12,2) NOT NULL,
  status TINYINT NOT NULL,
  merchant_reply VARCHAR(255) NULL,
  reject_reason VARCHAR(255) NULL,
  arbitrate_result TINYINT NULL,
  arbitrate_reason VARCHAR(255) NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (refund_id),
  UNIQUE KEY uk_refund_no (refund_no),
  KEY idx_refund_order (order_id),
  KEY idx_refund_user_status (user_id, status),
  KEY idx_refund_shop_status (shop_id, status),
  CONSTRAINT fk_refund_order FOREIGN KEY (order_id) REFERENCES `order` (order_id),
  CONSTRAINT fk_refund_order_item FOREIGN KEY (order_item_id) REFERENCES order_item (order_item_id),
  CONSTRAINT fk_refund_user FOREIGN KEY (user_id) REFERENCES user_account (user_id),
  CONSTRAINT fk_refund_shop FOREIGN KEY (shop_id) REFERENCES shop (shop_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE comment (
  comment_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  order_id BIGINT UNSIGNED NOT NULL,
  order_item_id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  product_id BIGINT UNSIGNED NOT NULL,
  shop_id BIGINT UNSIGNED NOT NULL,
  score INT NOT NULL,
  content VARCHAR(1024) NOT NULL,
  images_json JSON NULL,
  specs VARCHAR(255) NULL,
  is_anonymous TINYINT NOT NULL DEFAULT 0,
  status TINYINT NOT NULL DEFAULT 1,
  merchant_reply_content VARCHAR(1024) NULL,
  merchant_reply_time DATETIME NULL,
  append_content VARCHAR(1024) NULL,
  append_images_json JSON NULL,
  append_time DATETIME NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (comment_id),
  KEY idx_comment_product_time (product_id, create_time),
  KEY idx_comment_shop_time (shop_id, create_time),
  KEY idx_comment_user_time (user_id, create_time),
  KEY idx_comment_order_item (order_item_id),
  CONSTRAINT fk_comment_order FOREIGN KEY (order_id) REFERENCES `order` (order_id),
  CONSTRAINT fk_comment_order_item FOREIGN KEY (order_item_id) REFERENCES order_item (order_item_id),
  CONSTRAINT fk_comment_user FOREIGN KEY (user_id) REFERENCES user_account (user_id),
  CONSTRAINT fk_comment_product FOREIGN KEY (product_id) REFERENCES product (product_id),
  CONSTRAINT fk_comment_shop FOREIGN KEY (shop_id) REFERENCES shop (shop_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE banner (
  banner_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  title VARCHAR(128) NULL,
  image VARCHAR(512) NOT NULL,
  position VARCHAR(32) NOT NULL,
  link_type VARCHAR(32) NULL,
  link_value VARCHAR(255) NULL,
  sort INT NOT NULL DEFAULT 0,
  status TINYINT NOT NULL DEFAULT 1,
  start_time DATETIME NULL,
  end_time DATETIME NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (banner_id),
  KEY idx_banner_position_status (position, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE notice (
  notice_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  title VARCHAR(128) NOT NULL,
  content TEXT NOT NULL,
  type TINYINT NOT NULL DEFAULT 1,
  status TINYINT NOT NULL DEFAULT 1,
  sort INT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (notice_id),
  KEY idx_notice_status_type (status, type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE shop_finance_bill (
  bill_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  shop_id BIGINT UNSIGNED NOT NULL,
  type TINYINT NOT NULL,
  amount DECIMAL(14,2) NOT NULL,
  description VARCHAR(255) NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (bill_id),
  KEY idx_finance_shop_time (shop_id, create_time),
  CONSTRAINT fk_finance_shop FOREIGN KEY (shop_id) REFERENCES shop (shop_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO sys_role (role_id, role_name, role_code, description, status, sort) VALUES
  (1, '超级管理员', 'super_admin', '拥有所有权限', 1, 1);

INSERT INTO sys_permission (permission_id, parent_id, permission_name, permission_code, type, icon, sort) VALUES
  (1, 0, '系统管理', 'system', 'menu', 'icon-system', 1),
  (11, 1, '管理员管理', 'system:admin', 'menu', NULL, 1),
  (111, 11, '查看', 'system:admin:view', 'button', NULL, 1),
  (2, 0, '商家管理', 'merchant', 'menu', NULL, 2),
  (3, 0, '商品管理', 'product', 'menu', NULL, 3),
  (4, 0, '订单管理', 'order', 'menu', NULL, 4);

INSERT INTO sys_role_permission (role_id, permission_id) VALUES
  (1, 1), (1, 11), (1, 111), (1, 2), (1, 3), (1, 4);

INSERT INTO sys_admin (admin_id, username, password_hash, nickname, status) VALUES
  (1, 'admin', 'admin', '超级管理员', 1);

INSERT INTO sys_admin_role (admin_id, role_id) VALUES
  (1, 1);

INSERT INTO user_account (user_id, username, password_hash, phone, email, nickname, level, points, balance, status) VALUES
  (1001, 'user001', '123456', '13800138000', 'user@example.com', '小明', 2, 520, 199.50, 1);

INSERT INTO user_address (address_id, user_id, receiver_name, receiver_phone, province, city, district, detail, is_default) VALUES
  (1, 1001, '张三', '13800138000', '广东省', '深圳市', '南山区', '科技园南区A座', 1);

INSERT INTO merchant_application (application_id, shop_name, contact_name, contact_phone, email, password_hash, status, audit_time, audit_remark)
VALUES (1001, '热带果园', '张经理', '13800138001', 'shop@example.com', '123456', 1, NOW(), '资质齐全，准予入驻');

INSERT INTO merchant_account (merchant_id, application_id, shop_id, shop_name, contact_name, contact_phone, email, password_hash, status, audit_time, audit_remark)
VALUES (1001, 1001, NULL, '热带果园', '张经理', '13800138001', 'shop@example.com', '123456', 1, NOW(), '审核通过');

INSERT INTO shop (shop_id, merchant_id, shop_name, description, province, city, district, address, contact_name, contact_phone, business_hours, status, score, fan_count)
VALUES (10, 1001, '热带果园', '专营东南亚进口水果', '广东省', '深圳市', '南山区', '科技园北区', '张经理', '13800138001', '08:00-22:00', 1, 4.90, 3200);

UPDATE merchant_account SET shop_id = 10 WHERE merchant_id = 1001;

INSERT INTO product_category (category_id, category_name, parent_id, level, sort, status) VALUES
  (1, '热带水果', 0, 1, 1, 1),
  (11, '榴莲', 1, 2, 1, 1),
  (12, '芒果', 1, 2, 2, 1),
  (2, '温带水果', 0, 1, 2, 1),
  (21, '苹果', 2, 2, 1, 1),
  (22, '橙子', 2, 2, 2, 1);

INSERT INTO product (product_id, shop_id, product_name, category_id, image, price, original_price, cost_price, stock, sales, unit, weight, description, detail, tags_json, free_shipping, shipping_fee, score, comment_count, view_count, status)
VALUES
  (1, 10, '泰国金枕榴莲', 11, 'https://example.com/p1.jpg', 89.90, 120.00, 60.00, 100, 1520, '个', '3-4斤', '产地直发，新鲜香甜', '<html>详情页HTML内容</html>', JSON_ARRAY('新鲜','包邮'), 1, 0.00, 4.80, 856, 5600, 1);

INSERT INTO product_spec (spec_id, product_id, spec_name, options_json) VALUES
  (1, 1, '重量', JSON_ARRAY('3-4斤','5-6斤'));

INSERT INTO product_sku (sku_id, product_id, sku_code, price, stock, image, specs_json, status) VALUES
  (1, 1, 'SKU001', 89.90, 50, 'https://example.com/p1.jpg', JSON_OBJECT('重量','3-4斤'), 1),
  (2, 1, 'SKU002', 139.90, 50, 'https://example.com/p1.jpg', JSON_OBJECT('重量','5-6斤'), 1);

INSERT INTO product_image (product_id, image_url, sort) VALUES
  (1, 'https://example.com/p1_1.jpg', 1),
  (1, 'https://example.com/p1_2.jpg', 2),
  (1, 'https://example.com/p1_3.jpg', 3);

INSERT INTO product_hot_keyword (keyword_id, keyword, sort, status) VALUES
  (1, '榴莲', 1, 1),
  (2, '车厘子', 2, 1),
  (3, '苹果', 3, 1),
  (4, '橙子', 4, 1),
  (5, '草莓', 5, 1);

INSERT INTO banner (banner_id, title, image, position, link_type, link_value, sort, status) VALUES
  (1, '新鲜水果', 'https://example.com/banner1.jpg', 'home', 'product', '1', 1, 1);

INSERT INTO notice (notice_id, title, content, type, status, sort) VALUES
  (1, '春节配送时间调整通知', '详细内容...', 1, 1, 1);

SET FOREIGN_KEY_CHECKS = 1;
