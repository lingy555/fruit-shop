package com.lingnan.fruitshop.service.impl;

import com.lingnan.fruitshop.service.CartService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lingnan.fruitshop.common.exception.BizException;
import com.lingnan.fruitshop.dto.customer.cart.*;
import com.lingnan.fruitshop.dto.customer.cart.vo.CartAddResponse;
import com.lingnan.fruitshop.dto.customer.cart.vo.CartListResponse;
import com.lingnan.fruitshop.entity.*;
import com.lingnan.fruitshop.mapper.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class CartServiceImpl implements CartService {

    // ==================== 依赖注入区域 ====================
    // 注入购物车相关的数据访问层：购物车、商品、SKU、店铺等
    private final CartItemMapper cartItemMapper;
    private final ProductMapper productMapper;
    private final ProductSkuMapper productSkuMapper;
    private final ShopMapper shopMapper;
    private final ObjectMapper objectMapper;

    public CartServiceImpl(CartItemMapper cartItemMapper,
                       ProductMapper productMapper,
                       ProductSkuMapper productSkuMapper,
                       ShopMapper shopMapper,
                       ObjectMapper objectMapper) {
        this.cartItemMapper = cartItemMapper;
        this.productMapper = productMapper;
        this.productSkuMapper = productSkuMapper;
        this.shopMapper = shopMapper;
        this.objectMapper = objectMapper;
    }

    // ==================== 购物车列表查询核心逻辑 ====================
    // 查询用户购物车中的所有商品，按店铺分组显示
    @Override
    public CartListResponse list(long userId) {
        // 步骤1: 查询用户的所有购物车商品，按更新时间倒序
        List<CartItem> cartItems = cartItemMapper.selectList(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .orderByDesc(CartItem::getUpdateTime)
                .orderByDesc(CartItem::getCreateTime));

        // 步骤2: 如果购物车为空，直接返回空结果
        if (cartItems.isEmpty()) {
            return new CartListResponse(0, 0, BigDecimal.ZERO, List.of(), List.of());
        }

        // 步骤3: 收集需要关联查询的ID（商品ID、SKU ID、店铺ID）
        Set<Long> productIds = new HashSet<>();
        Set<Long> skuIds = new HashSet<>();
        Set<Long> shopIds = new HashSet<>();
        for (CartItem c : cartItems) {
            productIds.add(c.getProductId());
            skuIds.add(c.getSkuId());
            shopIds.add(c.getShopId());
        }

        Map<Long, Product> productMap = new HashMap<>();
        for (Product p : productMapper.selectBatchIds(productIds)) {
            productMap.put(p.getProductId(), p);
        }

        Map<Long, ProductSku> skuMap = new HashMap<>();
        for (ProductSku sku : productSkuMapper.selectBatchIds(skuIds)) {
            skuMap.put(sku.getSkuId(), sku);
        }

        Map<Long, Shop> shopMap = new HashMap<>();
        for (Shop s : shopMapper.selectBatchIds(shopIds)) {
            shopMap.put(s.getShopId(), s);
        }

        List<CartListResponse.Item> validItems = new ArrayList<>();
        List<CartListResponse.Item> invalidItems = new ArrayList<>();

        BigDecimal totalAmount = BigDecimal.ZERO;
        long selectedCount = 0;

        for (CartItem c : cartItems) {
            Product p = productMap.get(c.getProductId());
            ProductSku sku = skuMap.get(c.getSkuId());
            Shop shop = shopMap.get(c.getShopId());

            boolean computedValid = isValid(p, sku, c);
            boolean storedValid = c.getIsValid() != null && c.getIsValid() == 1;

            if (computedValid != storedValid) {
                cartItemMapper.update(null, new LambdaUpdateWrapper<CartItem>()
                        .eq(CartItem::getCartId, c.getCartId())
                        .eq(CartItem::getUserId, userId)
                        .set(CartItem::getIsValid, computedValid ? 1 : 0)
                        .set(CartItem::getSelected, computedValid ? c.getSelected() : 0));
                c.setIsValid(computedValid ? 1 : 0);
                if (!computedValid) {
                    c.setSelected(0);
                }
            }

            boolean selected = c.getSelected() != null && c.getSelected() == 1;
            int stock = sku == null || sku.getStock() == null ? 0 : sku.getStock();
            BigDecimal price = sku != null ? sku.getPrice() : (p == null ? BigDecimal.ZERO : p.getPrice());
            BigDecimal originalPrice = p == null ? null : p.getOriginalPrice();

            CartListResponse.Item item = new CartListResponse.Item(
                    c.getCartId(),
                    c.getProductId(),
                    c.getSkuId(),
                    p == null ? null : p.getProductName(),
                    sku != null && sku.getImage() != null ? sku.getImage() : (p == null ? null : p.getImage()),
                    price,
                    originalPrice,
                    sku == null ? null : specsText(sku.getSpecsJson()),
                    c.getQuantity(),
                    stock,
                    selected,
                    c.getIsValid() != null && c.getIsValid() == 1,
                    c.getShopId(),
                    shop == null ? null : shop.getShopName()
            );

            if (item.isValid()) {
                validItems.add(item);
                if (selected) {
                    selectedCount++;
                    totalAmount = totalAmount.add(price.multiply(BigDecimal.valueOf(c.getQuantity() == null ? 0 : c.getQuantity())));
                }
            } else {
                invalidItems.add(item);
            }
        }

        return new CartListResponse(cartItems.size(), selectedCount, totalAmount, validItems, invalidItems);
    }

    @Override
    public CartAddResponse add(long userId, CartAddRequest req) {
        Product product = productMapper.selectById(req.getProductId());
        if (product == null || product.getStatus() == null || product.getStatus() != 1) {
            throw BizException.notFound("商品不存在");
        }

        ProductSku sku = productSkuMapper.selectById(req.getSkuId());
        if (sku == null || sku.getStatus() == null || sku.getStatus() != 1) {
            throw BizException.notFound("SKU不存在");
        }
        if (!Objects.equals(sku.getProductId(), req.getProductId())) {
            throw BizException.badRequest("商品与SKU不匹配");
        }

        CartItem existing = cartItemMapper.selectOne(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .eq(CartItem::getSkuId, req.getSkuId())
                .last("limit 1"));

        if (existing != null) {
            int newQty = (existing.getQuantity() == null ? 0 : existing.getQuantity()) + req.getQuantity();
            existing.setQuantity(newQty);
            existing.setIsValid(1);
            cartItemMapper.updateById(existing);
        } else {
            CartItem item = new CartItem();
            item.setUserId(userId);
            item.setShopId(product.getShopId());
            item.setProductId(product.getProductId());
            item.setSkuId(sku.getSkuId());
            item.setQuantity(req.getQuantity());
            item.setSelected(1);
            item.setIsValid(1);
            cartItemMapper.insert(item);
            existing = item;
        }

        Long total = cartItemMapper.selectCount(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId));

        return new CartAddResponse(existing.getCartId(), total == null ? 0 : total);
    }

    @Override
    public void updateQuantity(long userId, CartUpdateQuantityRequest req) {
        CartItem item = cartItemMapper.selectOne(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getCartId, req.getCartId())
                .eq(CartItem::getUserId, userId)
                .last("limit 1"));
        if (item == null) {
            throw BizException.notFound("购物车商品不存在");
        }
        item.setQuantity(req.getQuantity());
        cartItemMapper.updateById(item);
    }

    @Override
    public void delete(long userId, CartDeleteRequest req) {
        cartItemMapper.delete(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .in(CartItem::getCartId, req.getCartIds()));
    }

    @Override
    public void selectAll(long userId, boolean selected) {
        cartItemMapper.update(null, new LambdaUpdateWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .eq(CartItem::getIsValid, 1)
                .set(CartItem::getSelected, selected ? 1 : 0));
        cartItemMapper.update(null, new LambdaUpdateWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .eq(CartItem::getIsValid, 0)
                .set(CartItem::getSelected, 0));
    }

    @Override
    public void select(long userId, CartSelectRequest req) {
        cartItemMapper.update(null, new LambdaUpdateWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .eq(CartItem::getIsValid, 1)
                .in(CartItem::getCartId, req.getCartIds())
                .set(CartItem::getSelected, req.getSelected() ? 1 : 0));
        cartItemMapper.update(null, new LambdaUpdateWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .eq(CartItem::getIsValid, 0)
                .in(CartItem::getCartId, req.getCartIds())
                .set(CartItem::getSelected, 0));
    }

    @Override
    public void clearInvalid(long userId) {
        cartItemMapper.delete(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .eq(CartItem::getIsValid, 0));
    }

    private boolean isValid(Product product, ProductSku sku, CartItem cartItem) {
        if (product == null || sku == null) {
            return false;
        }
        if (product.getStatus() == null || product.getStatus() != 1) {
            return false;
        }
        if (sku.getStatus() == null || sku.getStatus() != 1) {
            return false;
        }
        if (!Objects.equals(product.getProductId(), sku.getProductId())) {
            return false;
        }
        Integer stock = sku.getStock();
        if (stock == null || stock <= 0) {
            return false;
        }
        Integer qty = cartItem.getQuantity();
        if (qty == null || qty <= 0) {
            return false;
        }
        return qty <= stock;
    }

    private String specsText(String specsJson) {
        if (specsJson == null || specsJson.isBlank()) {
            return null;
        }
        try {
            Map<String, Object> map = objectMapper.readValue(specsJson, new TypeReference<Map<String, Object>>() {
            });
            if (map.isEmpty()) {
                return null;
            }
            return String.join("/", map.values().stream().map(String::valueOf).toList());
        } catch (Exception e) {
            return null;
        }
    }
}
