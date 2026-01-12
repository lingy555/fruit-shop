package com.lingnan.fruitshop.service.impl;

import com.lingnan.fruitshop.service.HomeService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingnan.fruitshop.dto.customer.common.vo.PageResponse;
import com.lingnan.fruitshop.dto.customer.home.vo.HomeIndexResponse;
import com.lingnan.fruitshop.entity.Banner;
import com.lingnan.fruitshop.entity.Notice;
import com.lingnan.fruitshop.entity.Product;
import com.lingnan.fruitshop.entity.ProductCategory;
import com.lingnan.fruitshop.entity.Shop;
import com.lingnan.fruitshop.mapper.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class HomeServiceImpl implements HomeService {

    private final BannerMapper bannerMapper;
    private final NoticeMapper noticeMapper;
    private final ProductCategoryMapper productCategoryMapper;
    private final ProductMapper productMapper;
    private final ShopMapper shopMapper;

    public HomeServiceImpl(BannerMapper bannerMapper,
                       NoticeMapper noticeMapper,
                       ProductCategoryMapper productCategoryMapper,
                       ProductMapper productMapper,
                       ShopMapper shopMapper) {
        this.bannerMapper = bannerMapper;
        this.noticeMapper = noticeMapper;
        this.productCategoryMapper = productCategoryMapper;
        this.productMapper = productMapper;
        this.shopMapper = shopMapper;
    }

    @Override
    public HomeIndexResponse index() {
        List<HomeIndexResponse.BannerItem> banners = bannerMapper.selectList(new LambdaQueryWrapper<Banner>()
                        .eq(Banner::getStatus, 1)
                        .eq(Banner::getPosition, "home")
                        .orderByAsc(Banner::getSort))
                .stream()
                .map(b -> new HomeIndexResponse.BannerItem(b.getBannerId(), b.getImage(), b.getTitle(), b.getLinkType(), b.getLinkValue(), b.getSort()))
                .toList();

        Map<Long, Long> categoryCountMap = new HashMap<>();
        for (Map<String, Object> row : productCategoryMapper.selectCategoryProductCount()) {
            Object cid = row.get("categoryId");
            Object cnt = row.get("productCount");
            if (cid != null && cnt != null) {
                categoryCountMap.put(Long.parseLong(String.valueOf(cid)), Long.parseLong(String.valueOf(cnt)));
            }
        }

        List<HomeIndexResponse.CategoryItem> categories = productCategoryMapper.selectList(new LambdaQueryWrapper<ProductCategory>()
                        .eq(ProductCategory::getStatus, 1)
                        .eq(ProductCategory::getLevel, 1)
                        .orderByAsc(ProductCategory::getSort))
                .stream()
                .map(c -> new HomeIndexResponse.CategoryItem(c.getCategoryId(), c.getCategoryName(), c.getIcon(), categoryCountMap.getOrDefault(c.getCategoryId(), 0L)))
                .toList();

        List<HomeIndexResponse.ProductItem> hotProducts = toProductItems(productMapper.selectList(new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, 1)
                .orderByDesc(Product::getSales)
                .last("limit 10")));

        List<HomeIndexResponse.ProductItem> newProducts = toProductItems(productMapper.selectList(new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, 1)
                .orderByDesc(Product::getCreateTime)
                .last("limit 10")));

        List<HomeIndexResponse.ProductItem> recommendProducts = toProductItems(productMapper.selectList(new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, 1)
                .orderByDesc(Product::getScore)
                .last("limit 10")));

        return new HomeIndexResponse(banners, categories, hotProducts, newProducts, recommendProducts);
    }

    @Override
    public List<HomeIndexResponse.BannerItem> banners(String position) {
        LambdaQueryWrapper<Banner> qw = new LambdaQueryWrapper<Banner>()
                .eq(Banner::getStatus, 1)
                .orderByAsc(Banner::getSort);
        if (position != null && !position.isBlank()) {
            qw.eq(Banner::getPosition, position);
        }
        return bannerMapper.selectList(qw)
                .stream()
                .map(b -> new HomeIndexResponse.BannerItem(b.getBannerId(), b.getImage(), b.getTitle(), b.getLinkType(), b.getLinkValue(), b.getSort()))
                .toList();
    }

    @Override
    public PageResponse<Notice> notices(int page, int pageSize) {
        Page<Notice> p = new Page<>(page, pageSize);
        Page<Notice> result = noticeMapper.selectPage(p, new LambdaQueryWrapper<Notice>()
                .eq(Notice::getStatus, 1)
                .orderByDesc(Notice::getSort)
                .orderByDesc(Notice::getCreateTime));
        return new PageResponse<>(result.getTotal(), result.getRecords());
    }

    private List<HomeIndexResponse.ProductItem> toProductItems(List<Product> products) {
        if (products.isEmpty()) {
            return List.of();
        }

        Set<Long> shopIds = new HashSet<>();
        for (Product p : products) {
            shopIds.add(p.getShopId());
        }
        Map<Long, Shop> shopMap = new HashMap<>();
        for (Shop s : shopMapper.selectBatchIds(shopIds)) {
            shopMap.put(s.getShopId(), s);
        }

        return products.stream().map(p -> new HomeIndexResponse.ProductItem(
                p.getProductId(),
                p.getProductName(),
                p.getImage(),
                p.getPrice(),
                p.getOriginalPrice(),
                p.getSales(),
                shopMap.get(p.getShopId()) == null ? null : shopMap.get(p.getShopId()).getShopName()
        )).toList();
    }
}
