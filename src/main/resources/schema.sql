CREATE TABLE IF NOT EXISTS product_image (
  image_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  product_id BIGINT UNSIGNED NOT NULL,
  image_url VARCHAR(512) NOT NULL,
  sort INT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (image_id),
  KEY idx_product_image_product_sort (product_id, sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                                