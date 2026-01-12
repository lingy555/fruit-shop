package com.lingnan.fruitshop.service.impl;

import com.lingnan.fruitshop.service.ShopService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingnan.fruitshop.common.exception.BizException;
import com.lingnan.fruitshop.dto.customer.common.vo.PageResponse;
import com.lingnan.fruitshop.dto.customer.product.vo.ProductListItemResponse;
import com.lingnan.fruitshop.dto.customer.shop.vo.MyFavoriteShopItemResponse;
import com.lingnan.fruitshop.dto.customer.shop.vo.ShopDetailResponse;
import com.lingnan.fruitshop.entity.Product;
import com.lingnan.fruitshop.entity.ProductCategory;
import com.lingnan.fruitshop.entity.ProductImage;
import com.lingnan.fruitshop.entity.Shop;
import com.lingnan.fruitshop.entity.ShopFavorite;
import com.lingnan.fruitshop.mapper.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ShopServiceImpl implements ShopService {

    private static final DateTimeFormatter DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ShopMapper shopMapper;
    private final ShopFavoriteMapper shopFavoriteMapper;
    private final ProductMapper productMapper;
    private final ProductCategoryMapper productCategoryMapper;
    private final ProductImageMapper productImageMapper;
    private final ObjectMapper objectMapper;

    public ShopServiceImpl(ShopMapper shopMapper,
                       ShopFavoriteMapper shopFavoriteMapper,
                       ProductMapper productMapper,
                       ProductCategoryMapper productCategoryMapper,
                       ProductImageMapper productImageMapper,
                       ObjectMapper objectMapper) {
        this.shopMapper = shopMapper;
        this.shopFavoriteMapper = shopFavoriteMapper;
        this.productMapper = productMapper;
        this.productCategoryMapper = productCategoryMapper;
        this.productImageMapper = productImageMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public ShopDetailResponse detail(long shopId, Optional<Long> optionalUserId) {
        Shop shop = shopMapper.selectById(shopId);
        if (shop == null) {
            throw BizException.notFound("店铺不存在");
        }

        Long productCount = productMapper.selectCount(new LambdaQueryWrapper<Product>()
                .eq(Product::getShopId, shopId)
                .eq(Product::getStatus, 1));

        boolean isFavorite = false;
        if (optionalUserId.isPresent()) {
            Long cnt = shopFavoriteMapper.selectCount(new LambdaQueryWrapper<ShopFavorite>()
                    .eq(ShopFavorite::getUserId, optionalUserId.get())
                    .eq(ShopFavorite::getShopId, shopId));
            isFavorite = cnt != null && cnt > 0;
        }

        return new ShopDetailResponse(
                shop.getShopId(),
                shop.getShopName(),
                shop.getLogo(),
                shop.getBanner(),
                shop.getDescription(),
                shop.getScore(),
                productCount == null ? 0 : productCount,
                shop.getTotalSalesCount() == null ? 0 : shop.getTotalSalesCount(),
                shop.getFanCount() == null ? 0 : shop.getFanCount(),
                isFavorite,
                shop.getCreateTime() == null ? null : shop.getCreateTime().format(DATETIME)
        );
    }

    @Override
    public PageResponse<ProductListItemResponse> shopProducts(long shopId, int page, int pageSize, String sortBy, String sortOrder) {
        LambdaQueryWrapper<Product> qw = new LambdaQueryWrapper<Product>()
                .eq(Product::getShopId, shopId)
                .eq(Product::getStatus, 1);

        boolean asc = "asc".equalsIgnoreCase(sortOrder);
        if (sortBy != null) {
            switch (sortBy) {
                case "price" -> qw.orderBy(true, asc, Product::getPrice);
                case "sales" -> qw.orderBy(true, asc, Product::getSales);
                case "createTime" -> qw.orderBy(true, asc, Product::getCreateTime);
                case "score" -> qw.orderBy(true, asc, Product::getScore);
                default -> qw.orderByDesc(Product::getCreateTime);
            }
        } else {
            qw.orderByDesc(Product::getCreateTime);
        }

        Page<Product> p = new Page<>(page, pageSize);
        Page<Product> result = productMapper.selectPage(p, qw);

        Set<Long> categoryIds = new HashSet<>();
        Set<Long> productIds = new HashSet<>();
        for (Product pr : result.getRecords()) {
            categoryIds.add(pr.getCategoryId());
            productIds.add(pr.getProductId());
        }

        Map<Long, ProductCategory> categoryMap = new HashMap<>();
        if (!categoryIds.isEmpty()) {
            for (ProductCategory c : productCategoryMapper.selectBatchIds(categoryIds)) {
                categoryMap.put(c.getCategoryId(), c);
            }
        }

        Map<Long, List<String>> imagesMap = new HashMap<>();
        if (!productIds.isEmpty()) {
            List<ProductImage> images = productImageMapper.selectList(new LambdaQueryWrapper<ProductImage>()
                    .in(ProductImage::getProductId, productIds)
                    .orderByAsc(ProductImage::getSort));
            for (ProductImage img : images) {
                imagesMap.computeIfAbsent(img.getProductId(), k -> new ArrayList<>()).add(img.getImageUrl());
            }
        }

        List<ProductListItemResponse> list = result.getRecords().stream().map(pr -> new ProductListItemResponse(
                pr.getProductId(),
                pr.getProductName(),
                pr.getCategoryId(),
                categoryMap.get(pr.getCategoryId()) == null ? null : categoryMap.get(pr.getCategoryId()).getCategoryName(),
                pr.getShopId(),
                null,
                pr.getImage(),
                imagesMap.getOrDefault(pr.getProductId(), List.of()),
                pr.getPrice(),
                pr.getOriginalPrice(),
                pr.getStock(),
                pr.getSales(),
                pr.getUnit(),
                pr.getWeight(),
                pr.getScore(),
                pr.getCommentCount(),
                parseStringArray(pr.getTagsJson()),
                false
        )).toList();

        return new PageResponse<>(result.getTotal(), list);
    }

    @Override
    public void favorite(long userId, long shopId) {
        Shop shop = shopMapper.selectById(shopId);
        if (shop == null) {
            throw BizException.notFound("店铺不存在");
        }

        Long cnt = shopFavoriteMapper.selectCount(new LambdaQueryWrapper<ShopFavorite>()
                .eq(ShopFavorite::getUserId, userId)
                .eq(ShopFavorite::getShopId, shopId));
        if (cnt != null && cnt > 0) {
            return;
        }

        ShopFavorite favorite = new ShopFavorite();
        favorite.setUserId(userId);
        favorite.setShopId(shopId);
        shopFavoriteMapper.insert(favorite);
    }

    @Override
    public void unfavorite(long userId, long shopId) {
        shopFavoriteMapper.delete(new LambdaQueryWrapper<ShopFavorite>()
                .eq(ShopFavorite::getUserId, userId)
                .eq(ShopFavorite::getShopId, shopId));
    }

    @Override
    public PageResponse<MyFavoriteShopItemResponse> myFavorites(long userId, int page, int pageSize) {
        Page<ShopFavorite> p = new Page<>(page, pageSize);
        Page<ShopFavorite> result = shopFavoriteMapper.selectPage(p, new LambdaQueryWrapper<ShopFavorite>()
                .eq(ShopFavorite::getUserId, userId)
                .orderByDesc(ShopFavorite::getCreateTime));

        if (result.getRecords().isEmpty()) {
            return new PageResponse<>(0, List.of());
        }

        Set<Long> shopIds = new HashSet<>();
        for (ShopFavorite f : result.getRecords()) {
            shopIds.add(f.getShopId());
        }

        Map<Long, Shop> shopMap = new HashMap<>();
        for (Shop s : shopMapper.selectBatchIds(shopIds)) {
            shopMap.put(s.getShopId(), s);
        }

        List<MyFavoriteShopItemResponse> list = result.getRecords().stream().map(f -> {
            Shop s = shopMap.get(f.getShopId());
            if (s == null) {
                return null;
            }
            return new MyFavoriteShopItemResponse(s.getShopId(), s.getShopName(), s.getLogo(), s.getScore(), s.getFanCount() == null ? 0 : s.getFanCount());
        }).filter(Objects::nonNull).toList();

        return new PageResponse<>(result.getTotal(), list);
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
}
