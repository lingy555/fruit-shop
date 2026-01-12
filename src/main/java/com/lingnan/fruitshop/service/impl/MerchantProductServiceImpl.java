package com.lingnan.fruitshop.service.impl;

import com.lingnan.fruitshop.service.MerchantProductService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lingnan.fruitshop.common.exception.BizException;
import com.lingnan.fruitshop.dto.customer.common.vo.PageResponse;
import com.lingnan.fruitshop.dto.customer.product.vo.CategoryTreeResponse;
import com.lingnan.fruitshop.dto.merchant.product.*;
import com.lingnan.fruitshop.dto.merchant.product.vo.*;
import com.lingnan.fruitshop.entity.*;
import com.lingnan.fruitshop.mapper.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class MerchantProductServiceImpl implements MerchantProductService {

    private static final DateTimeFormatter DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ProductMapper productMapper;
    private final ProductCategoryMapper productCategoryMapper;
    private final ProductImageMapper productImageMapper;
    private final ProductSpecMapper productSpecMapper;
    private final ProductSkuMapper productSkuMapper;
    private final OrderItemMapper orderItemMapper;
    private final ObjectMapper objectMapper;

    public MerchantProductServiceImpl(ProductMapper productMapper,
                                 ProductCategoryMapper productCategoryMapper,
                                 ProductImageMapper productImageMapper,
                                 ProductSpecMapper productSpecMapper,
                                 ProductSkuMapper productSkuMapper,
                                 OrderItemMapper orderItemMapper,
                                 ObjectMapper objectMapper) {
        this.productMapper = productMapper;
        this.productCategoryMapper = productCategoryMapper;
        this.productImageMapper = productImageMapper;
        this.productSpecMapper = productSpecMapper;
        this.productSkuMapper = productSkuMapper;
        this.orderItemMapper = orderItemMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public PageResponse<MerchantProductListItemResponse> list(long shopId, int page, int pageSize, Long categoryId, Integer status, String keyword, String sortBy, String sortOrder) {
        LambdaQueryWrapper<Product> qw = new LambdaQueryWrapper<Product>()
                .eq(Product::getShopId, shopId);

        if (categoryId != null) {
            qw.eq(Product::getCategoryId, categoryId);
        }
        if (status != null) {
            qw.eq(Product::getStatus, status);
        }
        if (keyword != null && !keyword.isBlank()) {
            qw.like(Product::getProductName, keyword);
        }

        boolean asc = "asc".equalsIgnoreCase(sortOrder);
        if (sortBy != null) {
            switch (sortBy) {
                case "createTime" -> qw.orderBy(true, asc, Product::getCreateTime);
                case "sales" -> qw.orderBy(true, asc, Product::getSales);
                case "stock" -> qw.orderBy(true, asc, Product::getStock);
                case "price" -> qw.orderBy(true, asc, Product::getPrice);
                default -> qw.orderByDesc(Product::getCreateTime);
            }
        } else {
            qw.orderByDesc(Product::getCreateTime);
        }

        Page<Product> p = new Page<>(page, pageSize);
        Page<Product> result = productMapper.selectPage(p, qw);

        Set<Long> categoryIds = new HashSet<>();
        for (Product pr : result.getRecords()) {
            categoryIds.add(pr.getCategoryId());
        }
        Map<Long, ProductCategory> categoryMap = new HashMap<>();
        categoryIds.removeIf(Objects::isNull);
        if (!categoryIds.isEmpty()) {
            for (ProductCategory c : productCategoryMapper.selectBatchIds(categoryIds)) {
                categoryMap.put(c.getCategoryId(), c);
            }
        }

        List<MerchantProductListItemResponse> list = result.getRecords().stream().map(pr -> new MerchantProductListItemResponse(
                pr.getProductId(),
                pr.getProductName(),
                pr.getCategoryId(),
                categoryMap.get(pr.getCategoryId()) == null ? null : categoryMap.get(pr.getCategoryId()).getCategoryName(),
                pr.getImage(),
                pr.getPrice(),
                pr.getOriginalPrice(),
                pr.getStock(),
                pr.getSales(),
                pr.getStatus(),
                statusText(pr.getStatus()),
                pr.getScore(),
                pr.getCommentCount(),
                pr.getViewCount(),
                pr.getCreateTime() == null ? null : pr.getCreateTime().format(DATETIME),
                pr.getUpdateTime() == null ? null : pr.getUpdateTime().format(DATETIME)
        )).toList();

        return new PageResponse<>(result.getTotal(), list);
    }

    @Override
    public MerchantProductDetailResponse detail(long shopId, long productId) {
        Product pr = productMapper.selectById(productId);
        if (pr == null || pr.getShopId() == null || pr.getShopId() != shopId) {
            throw BizException.notFound("商品不存在");
        }

        ProductCategory category = productCategoryMapper.selectById(pr.getCategoryId());

        List<String> images = productImageMapper.selectList(new LambdaQueryWrapper<ProductImage>()
                        .eq(ProductImage::getProductId, productId)
                        .orderByAsc(ProductImage::getSort))
                .stream().map(ProductImage::getImageUrl).toList();

        List<MerchantProductDetailResponse.SpecItem> specs = productSpecMapper.selectList(new LambdaQueryWrapper<ProductSpec>()
                        .eq(ProductSpec::getProductId, productId)
                        .orderByAsc(ProductSpec::getSpecId))
                .stream().map(s -> new MerchantProductDetailResponse.SpecItem(
                        s.getSpecId(),
                        s.getSpecName(),
                        parseStringArray(s.getOptionsJson())))
                .toList();

        List<MerchantProductDetailResponse.SkuItem> skuList = productSkuMapper.selectList(new LambdaQueryWrapper<ProductSku>()
                        .eq(ProductSku::getProductId, productId)
                        .orderByAsc(ProductSku::getSkuId))
                .stream().map(sku -> new MerchantProductDetailResponse.SkuItem(
                        sku.getSkuId(),
                        sku.getSkuCode(),
                        sku.getPrice(),
                        sku.getStock(),
                        sku.getImage(),
                        parseObject(sku.getSpecsJson())))
                .toList();

        MerchantProductDetailResponse.DeliveryInfo deliveryInfo = new MerchantProductDetailResponse.DeliveryInfo(
                pr.getFreeShipping() != null && pr.getFreeShipping() == 1,
                pr.getShippingFee(),
                "默认运费模板"
        );

        return new MerchantProductDetailResponse(
                pr.getProductId(),
                pr.getProductName(),
                pr.getCategoryId(),
                category == null ? null : category.getCategoryName(),
                pr.getImage(),
                images,
                pr.getVideoUrl(),
                pr.getPrice(),
                pr.getOriginalPrice(),
                pr.getCostPrice(),
                pr.getStock(),
                pr.getSales(),
                pr.getUnit(),
                pr.getWeight(),
                pr.getDescription(),
                pr.getDetail(),
                parseStringArray(pr.getTagsJson()),
                pr.getStatus(),
                specs,
                skuList,
                deliveryInfo,
                pr.getCreateTime() == null ? null : pr.getCreateTime().format(DATETIME)
        );
    }

    @Override
    public long add(long shopId, MerchantProductAddRequest req) {
        Product pr = new Product();
        pr.setShopId(shopId);
        pr.setProductName(req.getProductName());
        pr.setCategoryId(req.getCategoryId());
        pr.setImage(req.getImage());
        pr.setVideoUrl(req.getVideoUrl());
        pr.setPrice(req.getPrice());
        pr.setOriginalPrice(req.getOriginalPrice());
        pr.setCostPrice(req.getCostPrice());
        pr.setStock(req.getStock());
        pr.setUnit(req.getUnit());
        pr.setWeight(req.getWeight());
        pr.setDescription(req.getDescription());
        pr.setDetail(req.getDetail());
        pr.setTagsJson(writeJson(req.getTags()));
        pr.setFreeShipping(Boolean.TRUE.equals(req.getFreeShipping()) ? 1 : 0);
        pr.setShippingFee(req.getShippingFee());
        pr.setStatus(2);
        productMapper.insert(pr);

        saveImages(pr.getProductId(), req.getImages());
        saveSpecs(pr.getProductId(), req.getSpecifications());
        saveSkus(pr.getProductId(), req.getSkuList());

        return pr.getProductId();
    }

    @Override
    public void update(long shopId, long productId, MerchantProductUpdateRequest req) {
        Product pr = productMapper.selectById(productId);
        if (pr == null || pr.getShopId() == null || pr.getShopId() != shopId) {
            throw BizException.notFound("商品不存在");
        }

        productMapper.update(null, new LambdaUpdateWrapper<Product>()
                .eq(Product::getProductId, productId)
                .eq(Product::getShopId, shopId)
                .set(Product::getProductName, req.getProductName())
                .set(Product::getCategoryId, req.getCategoryId())
                .set(Product::getImage, req.getImage())
                .set(Product::getVideoUrl, req.getVideoUrl())
                .set(Product::getPrice, req.getPrice())
                .set(Product::getOriginalPrice, req.getOriginalPrice())
                .set(Product::getCostPrice, req.getCostPrice())
                .set(Product::getStock, req.getStock())
                .set(Product::getUnit, req.getUnit())
                .set(Product::getWeight, req.getWeight())
                .set(Product::getDescription, req.getDescription())
                .set(Product::getDetail, req.getDetail())
                .set(Product::getTagsJson, writeJson(req.getTags()))
                .set(Product::getFreeShipping, Boolean.TRUE.equals(req.getFreeShipping()) ? 1 : 0)
                .set(Product::getShippingFee, req.getShippingFee())
                .set(Product::getStatus, 2));

        productImageMapper.delete(new LambdaQueryWrapper<ProductImage>().eq(ProductImage::getProductId, productId));
        productSpecMapper.delete(new LambdaQueryWrapper<ProductSpec>().eq(ProductSpec::getProductId, productId));
        
        // 不删除SKU，而是更新状态为不可用，避免外键约束问题
        productSkuMapper.update(null, new LambdaUpdateWrapper<ProductSku>()
            .set(ProductSku::getStatus, 0)
            .set(ProductSku::getUpdateTime, LocalDateTime.now())
            .eq(ProductSku::getProductId, productId));

        saveImages(productId, req.getImages());
        saveSpecs(productId, req.getSpecifications());
        saveSkus(productId, req.getSkuList());
    }

    @Override
    public void delete(long shopId, long productId) {
        Product pr = productMapper.selectById(productId);
        if (pr == null || pr.getShopId() == null || pr.getShopId() != shopId) {
            throw BizException.notFound("商品不存在");
        }
        
        // 检查是否有订单引用此商品
        Long orderCount = orderItemMapper.selectCount(new LambdaQueryWrapper<OrderItem>()
            .eq(OrderItem::getProductId, productId));
        if (orderCount > 0) {
            // 如果有订单引用，只标记为删除状态而不真正删除
            productMapper.update(null, new LambdaUpdateWrapper<Product>()
                .set(Product::getStatus, -1)
                .set(Product::getUpdateTime, LocalDateTime.now())
                .eq(Product::getProductId, productId));
            
            // 同时标记相关SKU为不可用
            productSkuMapper.update(null, new LambdaUpdateWrapper<ProductSku>()
                .set(ProductSku::getStatus, 0)
                .set(ProductSku::getUpdateTime, LocalDateTime.now())
                .eq(ProductSku::getProductId, productId));
        } else {
            // 如果没有订单引用，可以安全删除
            productImageMapper.delete(new LambdaQueryWrapper<ProductImage>().eq(ProductImage::getProductId, productId));
            productSpecMapper.delete(new LambdaQueryWrapper<ProductSpec>().eq(ProductSpec::getProductId, productId));
            productSkuMapper.delete(new LambdaQueryWrapper<ProductSku>().eq(ProductSku::getProductId, productId));
            productMapper.deleteById(productId);
        }
    }

    @Override
    public void batchStatus(long shopId, MerchantProductBatchStatusRequest req) {
        productMapper.update(null, new LambdaUpdateWrapper<Product>()
                .eq(Product::getShopId, shopId)
                .in(Product::getProductId, req.getProductIds())
                .set(Product::getStatus, req.getStatus()));
    }

    @Override
    public void updateStock(long shopId, MerchantProductUpdateStockRequest req) {
        ProductSku sku = productSkuMapper.selectById(req.getSkuId());
        if (sku == null) {
            throw BizException.notFound("SKU不存在");
        }
        Product product = productMapper.selectById(sku.getProductId());
        if (product == null || product.getShopId() == null || product.getShopId() != shopId) {
            throw BizException.forbidden("无权限访问");
        }

        int newStock;
        int oldStock = sku.getStock() == null ? 0 : sku.getStock();
        if ("inc".equalsIgnoreCase(req.getType())) {
            newStock = oldStock + req.getStock();
        } else if ("dec".equalsIgnoreCase(req.getType())) {
            newStock = Math.max(0, oldStock - req.getStock());
        } else {
            newStock = req.getStock();
        }

        sku.setStock(newStock);
        productSkuMapper.updateById(sku);

        Integer sum = productSkuMapper.selectList(new LambdaQueryWrapper<ProductSku>().eq(ProductSku::getProductId, sku.getProductId()))
                .stream().map(ProductSku::getStock).filter(Objects::nonNull).mapToInt(Integer::intValue).sum();
        productMapper.update(null, new LambdaUpdateWrapper<Product>()
                .eq(Product::getProductId, sku.getProductId())
                .set(Product::getStock, sum));
    }

    @Override
    public List<CategoryTreeResponse> categories() {
        List<ProductCategory> all = productCategoryMapper.selectList(new LambdaQueryWrapper<ProductCategory>()
                .eq(ProductCategory::getStatus, 1)
                .orderByAsc(ProductCategory::getSort));

        Map<Long, List<CategoryTreeResponse>> childrenMap = new HashMap<>();
        Map<Long, CategoryTreeResponse> nodeMap = new HashMap<>();

        for (ProductCategory c : all) {
            CategoryTreeResponse node = new CategoryTreeResponse(
                    c.getCategoryId(),
                    c.getCategoryName(),
                    c.getIcon(),
                    c.getParentId(),
                    c.getLevel(),
                    c.getSort(),
                    new ArrayList<>(),
                    0L
            );
            nodeMap.put(node.getCategoryId(), node);
            childrenMap.computeIfAbsent(c.getParentId(), k -> new ArrayList<>()).add(node);
        }

        for (Map.Entry<Long, List<CategoryTreeResponse>> e : childrenMap.entrySet()) {
            Long parentId = e.getKey();
            CategoryTreeResponse parent = nodeMap.get(parentId);
            if (parent != null) {
                parent.setChildren(e.getValue());
            }
        }

        return childrenMap.getOrDefault(0L, List.of());
    }

    private void saveImages(long productId, List<String> images) {
        if (images == null) {
            return;
        }
        int sort = 1;
        for (String url : images) {
            if (url == null || url.isBlank()) {
                continue;
            }
            ProductImage img = new ProductImage();
            img.setProductId(productId);
            img.setImageUrl(url);
            img.setSort(sort++);
            productImageMapper.insert(img);
        }
    }

    private void saveSpecs(long productId, List<MerchantProductAddRequest.Specification> specs) {
        if (specs == null) {
            return;
        }
        for (MerchantProductAddRequest.Specification s : specs) {
            ProductSpec ps = new ProductSpec();
            ps.setProductId(productId);
            ps.setSpecName(s.getSpecName());
            ps.setOptionsJson(writeJson(s.getOptions()));
            productSpecMapper.insert(ps);
        }
    }

    private void saveSkus(long productId, List<MerchantProductAddRequest.Sku> skus) {
        if (skus == null || skus.isEmpty()) {
            Product p = productMapper.selectById(productId);
            if (p == null) {
                return;
            }
            ProductSku sku = new ProductSku();
            sku.setProductId(productId);
            sku.setSkuCode("DEFAULT");
            sku.setPrice(p.getPrice());
            sku.setStock(p.getStock() == null ? 0 : p.getStock());
            sku.setImage(p.getImage());
            sku.setSpecsJson(writeJson(Map.of()));
            sku.setStatus(1);
            productSkuMapper.insert(sku);
            return;
        }
        for (MerchantProductAddRequest.Sku skuReq : skus) {
            ProductSku sku = new ProductSku();
            sku.setProductId(productId);
            sku.setSkuCode(skuReq.getSkuCode());
            sku.setPrice(skuReq.getPrice());
            sku.setStock(skuReq.getStock());
            sku.setImage(skuReq.getImage());
            sku.setSpecsJson(writeJson(skuReq.getSpecs()));
            sku.setStatus(1);
            productSkuMapper.insert(sku);
        }
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

    private String writeJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return null;
        }
    }

    private String statusText(Integer status) {
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
}
