package com.lingnan.fruitshop.service.impl;

import com.lingnan.fruitshop.service.AdminProductService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lingnan.fruitshop.common.exception.BizException;
import com.lingnan.fruitshop.dto.admin.product.AdminProductAuditRequest;
import com.lingnan.fruitshop.dto.admin.product.AdminProductForceOfflineRequest;
import com.lingnan.fruitshop.dto.admin.product.vo.AdminProductDetailResponse;
import com.lingnan.fruitshop.dto.admin.product.vo.AdminProductListResponse;
import com.lingnan.fruitshop.entity.*;
import com.lingnan.fruitshop.mapper.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class AdminProductServiceImpl implements AdminProductService {

    private static final DateTimeFormatter DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ProductMapper productMapper;
    private final ProductCategoryMapper productCategoryMapper;
    private final ShopMapper shopMapper;
    private final ProductImageMapper productImageMapper;
    private final ProductSpecMapper productSpecMapper;
    private final ProductSkuMapper productSkuMapper;
    private final ObjectMapper objectMapper;

    public AdminProductServiceImpl(ProductMapper productMapper,
                              ProductCategoryMapper productCategoryMapper,
                              ShopMapper shopMapper,
                              ProductImageMapper productImageMapper,
                              ProductSpecMapper productSpecMapper,
                              ProductSkuMapper productSkuMapper,
                              ObjectMapper objectMapper) {
        this.productMapper = productMapper;
        this.productCategoryMapper = productCategoryMapper;
        this.shopMapper = shopMapper;
        this.productImageMapper = productImageMapper;
        this.productSpecMapper = productSpecMapper;
        this.productSkuMapper = productSkuMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public AdminProductListResponse list(int page, int pageSize, String keyword, Long categoryId, Long shopId, Integer status, BigDecimal minPrice, BigDecimal maxPrice) {
        LambdaQueryWrapper<Product> qw = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            qw.like(Product::getProductName, keyword);
        }
        if (categoryId != null) {
            qw.eq(Product::getCategoryId, categoryId);
        }
        if (shopId != null) {
            qw.eq(Product::getShopId, shopId);
        }
        if (status != null) {
            qw.eq(Product::getStatus, status);
        }
        if (minPrice != null) {
            qw.ge(Product::getPrice, minPrice);
        }
        if (maxPrice != null) {
            qw.le(Product::getPrice, maxPrice);
        }
        qw.orderByDesc(Product::getCreateTime);

        Page<Product> p = new Page<>(page, pageSize);
        Page<Product> result = productMapper.selectPage(p, qw);

        AdminProductListResponse.StatusCount statusCount = statusCount();
        if (result.getRecords().isEmpty()) {
            return new AdminProductListResponse(0, statusCount, List.of());
        }

        Set<Long> categoryIds = new HashSet<>();
        Set<Long> shopIds = new HashSet<>();
        for (Product pr : result.getRecords()) {
            if (pr.getCategoryId() != null) {
                categoryIds.add(pr.getCategoryId());
            }
            if (pr.getShopId() != null) {
                shopIds.add(pr.getShopId());
            }
        }

        Map<Long, ProductCategory> categoryMap = new HashMap<>();
        if (!categoryIds.isEmpty()) {
            for (ProductCategory c : productCategoryMapper.selectBatchIds(categoryIds)) {
                categoryMap.put(c.getCategoryId(), c);
            }
        }

        Map<Long, Shop> shopMap = new HashMap<>();
        if (!shopIds.isEmpty()) {
            for (Shop s : shopMapper.selectBatchIds(shopIds)) {
                shopMap.put(s.getShopId(), s);
            }
        }

        List<AdminProductListResponse.Item> list = result.getRecords().stream().map(pr -> {
            ProductCategory c = pr.getCategoryId() == null ? null : categoryMap.get(pr.getCategoryId());
            Shop s = pr.getShopId() == null ? null : shopMap.get(pr.getShopId());
            return new AdminProductListResponse.Item(
                    pr.getProductId(),
                    pr.getProductName(),
                    pr.getCategoryId(),
                    c == null ? null : c.getCategoryName(),
                    pr.getShopId(),
                    s == null ? null : s.getShopName(),
                    pr.getImage(),
                    pr.getPrice(),
                    pr.getOriginalPrice(),
                    pr.getStock(),
                    pr.getSales(),
                    pr.getStatus(),
                    productStatusText(pr.getStatus()),
                    pr.getScore(),
                    pr.getCommentCount(),
                    pr.getViewCount(),
                    format(pr.getCreateTime())
            );
        }).toList();

        return new AdminProductListResponse(result.getTotal(), statusCount, list);
    }

    @Override
    public AdminProductDetailResponse detail(long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw BizException.notFound("商品不存在");
        }

        ProductCategory category = product.getCategoryId() == null ? null : productCategoryMapper.selectById(product.getCategoryId());
        Shop shop = product.getShopId() == null ? null : shopMapper.selectById(product.getShopId());

        List<String> images = productImageMapper.selectList(new LambdaQueryWrapper<ProductImage>()
                        .eq(ProductImage::getProductId, productId)
                        .orderByAsc(ProductImage::getSort))
                .stream().map(ProductImage::getImageUrl).toList();

        List<AdminProductDetailResponse.SpecItem> specs = productSpecMapper.selectList(new LambdaQueryWrapper<ProductSpec>()
                        .eq(ProductSpec::getProductId, productId)
                        .orderByAsc(ProductSpec::getSpecId))
                .stream().map(s -> new AdminProductDetailResponse.SpecItem(s.getSpecId(), s.getSpecName(), parseStringArray(s.getOptionsJson())))
                .toList();

        List<AdminProductDetailResponse.SkuItem> skuList = productSkuMapper.selectList(new LambdaQueryWrapper<ProductSku>()
                        .eq(ProductSku::getProductId, productId)
                        .orderByAsc(ProductSku::getSkuId))
                .stream().map(sku -> new AdminProductDetailResponse.SkuItem(
                        sku.getSkuId(),
                        sku.getSkuCode(),
                        sku.getPrice(),
                        sku.getStock(),
                        sku.getImage(),
                        sku.getStatus(),
                        parseObject(sku.getSpecsJson())
                )).toList();

        AdminProductDetailResponse.DeliveryInfo deliveryInfo = new AdminProductDetailResponse.DeliveryInfo(
                product.getFreeShipping() != null && product.getFreeShipping() == 1,
                product.getShippingFee(),
                "默认运费"
        );

        return new AdminProductDetailResponse(
                product.getProductId(),
                product.getProductName(),
                product.getCategoryId(),
                category == null ? null : category.getCategoryName(),
                product.getShopId(),
                shop == null ? null : shop.getShopName(),
                product.getImage(),
                images,
                product.getVideoUrl(),
                product.getPrice(),
                product.getOriginalPrice(),
                product.getCostPrice(),
                product.getStock(),
                product.getSales(),
                product.getUnit(),
                product.getWeight(),
                product.getDescription(),
                product.getDetail(),
                parseStringArray(product.getTagsJson()),
                product.getStatus(),
                productStatusText(product.getStatus()),
                product.getAuditRemark(),
                product.getScore(),
                product.getCommentCount(),
                product.getViewCount(),
                specs,
                skuList,
                deliveryInfo,
                format(product.getCreateTime())
        );
    }

    @Override
    public void audit(AdminProductAuditRequest req) {
        Product product = productMapper.selectById(req.getProductId());
        if (product == null) {
            throw BizException.notFound("商品不存在");
        }

        productMapper.update(null, new LambdaUpdateWrapper<Product>()
                .eq(Product::getProductId, req.getProductId())
                .set(Product::getStatus, req.getStatus())
                .set(Product::getAuditRemark, req.getAuditRemark()));
    }

    @Override
    public void forceOffline(AdminProductForceOfflineRequest req) {
        Product product = productMapper.selectById(req.getProductId());
        if (product == null) {
            throw BizException.notFound("商品不存在");
        }

        productMapper.update(null, new LambdaUpdateWrapper<Product>()
                .eq(Product::getProductId, req.getProductId())
                .set(Product::getStatus, 0)
                .set(Product::getAuditRemark, req.getReason()));
    }

    @Override
    public void delete(long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            return;
        }

        productImageMapper.delete(new LambdaQueryWrapper<ProductImage>().eq(ProductImage::getProductId, productId));
        productSpecMapper.delete(new LambdaQueryWrapper<ProductSpec>().eq(ProductSpec::getProductId, productId));
        productSkuMapper.delete(new LambdaQueryWrapper<ProductSku>().eq(ProductSku::getProductId, productId));
        productMapper.deleteById(productId);
    }

    private AdminProductListResponse.StatusCount statusCount() {
        long offline = safeLong(productMapper.selectCount(new LambdaQueryWrapper<Product>().eq(Product::getStatus, 0)));
        long online = safeLong(productMapper.selectCount(new LambdaQueryWrapper<Product>().eq(Product::getStatus, 1)));
        long pending = safeLong(productMapper.selectCount(new LambdaQueryWrapper<Product>().eq(Product::getStatus, 2)));
        long rejected = safeLong(productMapper.selectCount(new LambdaQueryWrapper<Product>().eq(Product::getStatus, 3)));
        return new AdminProductListResponse.StatusCount(pending, online, offline, rejected);
    }

    private String productStatusText(Integer status) {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case 0 -> "已下架";
            case 1 -> "已上架";
            case 2 -> "待审核";
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

    private List<String> parseStringArray(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            return List.of();
        }
    }

    private Map<String, Object> parseObject(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            return Map.of();
        }
    }
}
