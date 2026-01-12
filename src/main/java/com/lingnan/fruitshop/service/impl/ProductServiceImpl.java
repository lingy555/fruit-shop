package com.lingnan.fruitshop.service.impl;

import com.lingnan.fruitshop.service.ProductService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lingnan.fruitshop.common.exception.BizException;
import com.lingnan.fruitshop.dto.customer.common.vo.PageResponse;
import com.lingnan.fruitshop.dto.customer.product.vo.*;
import com.lingnan.fruitshop.entity.*;
import com.lingnan.fruitshop.mapper.*;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ProductServiceImpl implements ProductService {

    // ==================== 常量定义 ====================
    private static final DateTimeFormatter DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ==================== 依赖注入区域 ====================
    // 注入商品相关的所有数据访问层：分类、商品、图片、规格、SKU等
    private final ProductCategoryMapper productCategoryMapper;
    private final ProductMapper productMapper;
    private final ProductImageMapper productImageMapper;
    private final ProductSpecMapper productSpecMapper;
    private final ProductSkuMapper productSkuMapper;
    private final ProductHotKeywordMapper productHotKeywordMapper;
    private final ProductSearchHistoryMapper productSearchHistoryMapper;
    private final CommentMapper commentMapper;
    private final ShopMapper shopMapper;
    private final ObjectMapper objectMapper;

    public ProductServiceImpl(ProductCategoryMapper productCategoryMapper,
                          ProductMapper productMapper,
                          ProductImageMapper productImageMapper,
                          ProductSpecMapper productSpecMapper,
                          ProductSkuMapper productSkuMapper,
                          ProductHotKeywordMapper productHotKeywordMapper,
                          ProductSearchHistoryMapper productSearchHistoryMapper,
                          CommentMapper commentMapper,
                          ShopMapper shopMapper,
                          ObjectMapper objectMapper) {
        this.productCategoryMapper = productCategoryMapper;
        this.productMapper = productMapper;
        this.productImageMapper = productImageMapper;
        this.productSpecMapper = productSpecMapper;
        this.productSkuMapper = productSkuMapper;
        this.productHotKeywordMapper = productHotKeywordMapper;
        this.productSearchHistoryMapper = productSearchHistoryMapper;
        this.commentMapper = commentMapper;
        this.shopMapper = shopMapper;
        this.objectMapper = objectMapper;
    }

    // ==================== 商品分类树构建 ====================
/**
 * 构建商品分类树结构
 * 该方法用于查询所有启用状态的分类，并构建一个树形结构，同时统计每个分类下的商品数量
 * @return 返回一个分类树的列表，每个节点包含分类信息及其子分类
 */
    @Override
    public List<CategoryTreeResponse> categories() {
        // 步骤1: 查询所有启用状态的分类，按排序字段排序
    // 使用LambdaQueryWrapper构建查询条件，查询状态为1（启用）的分类，并按sort字段升序排列
        List<ProductCategory> all = productCategoryMapper.selectList(new LambdaQueryWrapper<ProductCategory>()
                .eq(ProductCategory::getStatus, 1)
                .orderByAsc(ProductCategory::getSort));

        // 步骤2: 统计每个分类下的商品数量
    // 创建一个Map用于存储分类ID和对应的商品数量
        Map<Long, Long> categoryCountMap = new HashMap<>();
    // 遍历查询结果，将分类ID和商品数量存入Map
        for (Map<String, Object> row : productCategoryMapper.selectCategoryProductCount()) {
            Object cid = row.get("categoryId");
            Object cnt = row.get("productCount");
        // 确保分类ID和商品数量不为空，然后转换为Long类型存入Map
            if (cid != null && cnt != null) {
                categoryCountMap.put(Long.parseLong(String.valueOf(cid)), Long.parseLong(String.valueOf(cnt)));
            }
        }

        // 步骤3: 构建父子关系的映射表
    // 创建两个Map，一个用于存储子节点列表，一个用于存储所有节点
        Map<Long, List<CategoryTreeResponse>> childrenMap = new HashMap<>();
        Map<Long, CategoryTreeResponse> nodeMap = new HashMap<>();

        // 步骤4: 遍历所有分类，创建树节点
    // 遍历所有分类，为每个分类创建一个树节点
        for (ProductCategory c : all) {
        // 创建分类树节点，包含分类ID、名称、图标、父ID、层级、排序、子节点列表和商品数量
            CategoryTreeResponse node = new CategoryTreeResponse(
                    c.getCategoryId(),
                    c.getCategoryName(),
                    c.getIcon(),
                    c.getParentId(),
                    c.getLevel(),
                    c.getSort(),
                    new ArrayList<>(),
                    categoryCountMap.getOrDefault(c.getCategoryId(), 0L)
            );
        // 将节点存入nodeMap
            nodeMap.put(node.getCategoryId(), node);
        // 将节点添加到其父节点的子节点列表中
            childrenMap.computeIfAbsent(c.getParentId(), k -> new ArrayList<>()).add(node);
        }

    // 遍历childrenMap，为每个父节点设置其子节点列表
        for (Map.Entry<Long, List<CategoryTreeResponse>> e : childrenMap.entrySet()) {
            Long parentId = e.getKey();
            CategoryTreeResponse parent = nodeMap.get(parentId);
        // 如果父节点存在，设置其子节点列表
            if (parent != null) {
                parent.setChildren(e.getValue());
            }
        }

    // 返回根节点的子节点列表（父ID为0的节点）
        return childrenMap.getOrDefault(0L, List.of());
    }

    // ==================== 商品列表查询核心逻辑 ====================
    // 支持多维度筛选：分类、店铺、关键词、价格区间、库存状态、排序等
    @Override
    public PageResponse<ProductListItemResponse> list(int page, int pageSize,
                                                     Long categoryId,
                                                     Long shopId,
                                                     String keyword,
                                                     String sortBy,
                                                     String sortOrder,
                                                     Double minPrice,
                                                     Double maxPrice,
                                                     Boolean onlyStock) {

        // 步骤1: 构建查询条件 - 只查询启用状态的商品
        LambdaQueryWrapper<Product> qw = new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, 1);

        // 步骤2: 添加可选的筛选条件
        if (categoryId != null) {
            qw.eq(Product::getCategoryId, categoryId);           // 分类筛选
        }
        if (shopId != null) {
            qw.eq(Product::getShopId, shopId);                 // 店铺筛选
        }
        if (keyword != null && !keyword.isBlank()) {
            qw.like(Product::getProductName, keyword);          // 关键词搜索
        }
        if (minPrice != null) {
            qw.ge(Product::getPrice, minPrice);                // 最低价格
        }
        if (maxPrice != null) {
            qw.le(Product::getPrice, maxPrice);                // 最高价格
        }
        if (onlyStock != null && onlyStock) {
            qw.gt(Product::getStock, 0);                       // 仅显示有库存商品
        }

        // 步骤3: 处理排序逻辑
        boolean asc = "asc".equalsIgnoreCase(sortOrder);
        if (sortBy != null) {
            switch (sortBy) {
                case "price" -> qw.orderBy(true, asc, Product::getPrice);        // 按价格排序
                case "sales" -> qw.orderBy(true, asc, Product::getSales);        // 按销量排序
                case "createTime" -> qw.orderBy(true, asc, Product::getCreateTime); // 按创建时间排序
                case "score" -> qw.orderBy(true, asc, Product::getScore);        // 按评分排序
                default -> qw.orderByDesc(Product::getCreateTime);                // 默认按时间倒序
            }
        } else {
            qw.orderByDesc(Product::getCreateTime);                    // 默认排序
        }

        // 步骤4: 执行分页查询
        Page<Product> p = new Page<>(page, pageSize);
        Page<Product> result = productMapper.selectPage(p, qw);

        // 步骤5: 收集需要关联查询的ID（分类ID、店铺ID等）
        Set<Long> categoryIds = new HashSet<>();
        Set<Long> shopIds = new HashSet<>();
        Set<Long> productIds = new HashSet<>();
        for (Product pr : result.getRecords()) {
            categoryIds.add(pr.getCategoryId());
            shopIds.add(pr.getShopId());
            productIds.add(pr.getProductId());
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
                shopMap.get(pr.getShopId()) == null ? null : shopMap.get(pr.getShopId()).getShopName(),
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
    public ProductDetailResponse detail(Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null || product.getStatus() == null || product.getStatus() != 1) {
            throw BizException.notFound("商品不存在");
        }

        ProductCategory category = productCategoryMapper.selectById(product.getCategoryId());
        Shop shop = shopMapper.selectById(product.getShopId());

        long productCount = productMapper.selectCount(new LambdaQueryWrapper<Product>()
                .eq(Product::getShopId, product.getShopId())
                .eq(Product::getStatus, 1));
        long fanCount = shop == null || shop.getFanCount() == null ? 0 : shop.getFanCount();

        List<String> images = productImageMapper.selectList(new LambdaQueryWrapper<ProductImage>()
                        .eq(ProductImage::getProductId, productId)
                        .orderByAsc(ProductImage::getSort))
                .stream().map(ProductImage::getImageUrl).toList();

        List<ProductDetailResponse.SpecItem> specs = productSpecMapper.selectList(new LambdaQueryWrapper<ProductSpec>()
                        .eq(ProductSpec::getProductId, productId)
                        .orderByAsc(ProductSpec::getSpecId))
                .stream().map(s -> new ProductDetailResponse.SpecItem(s.getSpecId(), s.getSpecName(), parseStringArray(s.getOptionsJson())))
                .toList();

        List<ProductDetailResponse.SkuItem> skuList = productSkuMapper.selectList(new LambdaQueryWrapper<ProductSku>()
                        .eq(ProductSku::getProductId, productId)
                        .eq(ProductSku::getStatus, 1)
                        .orderByAsc(ProductSku::getSkuId))
                .stream().map(sku -> new ProductDetailResponse.SkuItem(
                        sku.getSkuId(),
                        sku.getPrice(),
                        sku.getStock(),
                        parseObject(sku.getSpecsJson())))
                .toList();

        ProductDetailResponse.DeliveryInfo deliveryInfo = new ProductDetailResponse.DeliveryInfo(
                product.getFreeShipping() != null && product.getFreeShipping() == 1,
                product.getShippingFee(),
                "预计明天送达"
        );

        return new ProductDetailResponse(
                product.getProductId(),
                product.getProductName(),
                product.getCategoryId(),
                category == null ? null : category.getCategoryName(),
                product.getShopId(),
                shop == null ? null : shop.getShopName(),
                shop == null ? null : shop.getLogo(),
                shop == null ? null : shop.getScore(),
                productCount,
                fanCount,
                product.getImage(),
                images,
                product.getVideoUrl(),
                product.getPrice(),
                product.getOriginalPrice(),
                product.getStock(),
                product.getSales(),
                product.getUnit(),
                product.getWeight(),
                product.getDescription(),
                product.getDetail(),
                product.getScore(),
                product.getCommentCount(),
                parseStringArray(product.getTagsJson()),
                specs,
                skuList,
                deliveryInfo,
                false,
                0,
                product.getViewCount() == null ? 0 : product.getViewCount(),
                product.getCreateTime() == null ? null : product.getCreateTime().format(DATETIME)
        );
    }

    @Override
    public ProductCommentsResponse comments(Long productId, int page, int pageSize, Integer type) {
        LambdaQueryWrapper<Comment> qw = new LambdaQueryWrapper<Comment>()
                .eq(Comment::getProductId, productId)
                .eq(Comment::getStatus, 1)
                .orderByDesc(Comment::getCreateTime);

        if (type != null) {
            if (type == 1) {
                qw.ge(Comment::getScore, 4);
            } else if (type == 2) {
                qw.eq(Comment::getScore, 3);
            } else if (type == 3) {
                qw.le(Comment::getScore, 2);
            } else if (type == 4) {
                qw.isNotNull(Comment::getImagesJson).ne(Comment::getImagesJson, "[]");
            }
        }

        Page<Comment> p = new Page<>(page, pageSize);
        Page<Comment> result = commentMapper.selectPage(p, qw);

        long totalAll = commentMapper.selectCount(new LambdaQueryWrapper<Comment>().eq(Comment::getProductId, productId).eq(Comment::getStatus, 1));
        long good = commentMapper.selectCount(new LambdaQueryWrapper<Comment>().eq(Comment::getProductId, productId).eq(Comment::getStatus, 1).ge(Comment::getScore, 4));
        long medium = commentMapper.selectCount(new LambdaQueryWrapper<Comment>().eq(Comment::getProductId, productId).eq(Comment::getStatus, 1).eq(Comment::getScore, 3));
        long bad = commentMapper.selectCount(new LambdaQueryWrapper<Comment>().eq(Comment::getProductId, productId).eq(Comment::getStatus, 1).le(Comment::getScore, 2));
        long imageCount = commentMapper.selectCount(new LambdaQueryWrapper<Comment>().eq(Comment::getProductId, productId).eq(Comment::getStatus, 1).isNotNull(Comment::getImagesJson).ne(Comment::getImagesJson, "[]"));

        double goodRate = totalAll == 0 ? 0D : (good * 100.0 / totalAll);
        ProductCommentsResponse.Summary summary = new ProductCommentsResponse.Summary(goodRate, good, medium, bad, imageCount);

        List<ProductCommentsResponse.Item> list = result.getRecords().stream().map(c -> {
            String username = c.getIsAnonymous() != null && c.getIsAnonymous() == 1 ? "匿名用户" : "用户***";
            List<String> images = parseStringArray(c.getImagesJson());
            ProductCommentsResponse.Reply reply = null;
            if (c.getMerchantReplyContent() != null) {
                reply = new ProductCommentsResponse.Reply(c.getMerchantReplyContent(), c.getMerchantReplyTime() == null ? null : c.getMerchantReplyTime().format(DATETIME));
            }
            return new ProductCommentsResponse.Item(
                    c.getCommentId(),
                    c.getUserId(),
                    username,
                    null,
                    c.getScore(),
                    c.getContent(),
                    images,
                    c.getSpecs(),
                    c.getCreateTime() == null ? null : c.getCreateTime().format(DATETIME),
                    reply
            );
        }).toList();

        return new ProductCommentsResponse(result.getTotal(), summary, list);
    }

    @Override
    public List<String> hotKeywords() {
        return productHotKeywordMapper.selectList(new LambdaQueryWrapper<ProductHotKeyword>()
                        .eq(ProductHotKeyword::getStatus, 1)
                        .orderByAsc(ProductHotKeyword::getSort))
                .stream().map(ProductHotKeyword::getKeyword).toList();
    }

    @Override
    public List<String> searchHistory(long userId) {
        return productSearchHistoryMapper.selectList(new LambdaQueryWrapper<ProductSearchHistory>()
                        .eq(ProductSearchHistory::getUserId, userId)
                        .orderByDesc(ProductSearchHistory::getLastTime)
                        .last("limit 20"))
                .stream().map(ProductSearchHistory::getKeyword).toList();
    }

    @Override
    public void clearSearchHistory(long userId) {
        productSearchHistoryMapper.delete(new LambdaQueryWrapper<ProductSearchHistory>()
                .eq(ProductSearchHistory::getUserId, userId));
    }

/**
 * 解析JSON字符串为字符串列表
 * @param json 需要解析的JSON格式字符串
 * @return 解析后的字符串列表，如果解析失败或输入为空则返回空列表
 */
    private List<String> parseStringArray(String json) {
    // 检查输入是否为null或空白字符串
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
        // 使用ObjectMapper将JSON字符串解析为List<String>类型
            return objectMapper.readValue(json, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
        // 捕获所有异常并返回空列表
            return List.of();
        }
    }

/**
 * 解析JSON字符串为Map对象
 * @param json 需要解析的JSON字符串
 * @return 解析后的Map对象，如果输入为空或解析失败则返回空Map
 */
    private Map<String, Object> parseObject(String json) {
        // 检查输入是否为null或空字符串
        if (json == null || json.isBlank()) {
            return Map.of(); // 返回空Map
        }
        try {
            // 使用objectMapper将JSON字符串解析为Map<String, Object>类型
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            // 捕获所有异常并返回空Map，确保方法不会抛出异常
            return Map.of();
        }
    }
}
