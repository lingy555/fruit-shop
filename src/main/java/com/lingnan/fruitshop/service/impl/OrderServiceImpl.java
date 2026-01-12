package com.lingnan.fruitshop.service.impl;

import com.lingnan.fruitshop.service.OrderService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lingnan.fruitshop.common.exception.BizException;
import com.lingnan.fruitshop.dto.customer.common.vo.PageResponse;
import com.lingnan.fruitshop.dto.customer.order.*;
import com.lingnan.fruitshop.dto.customer.order.vo.*;
import com.lingnan.fruitshop.entity.*;
import com.lingnan.fruitshop.mapper.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    // ==================== 常量定义 ====================
    private static final DateTimeFormatter DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // 订单状态常量定义
    private static final int STATUS_WAIT_PAY = 1;      // 待付款
    private static final int STATUS_WAIT_DELIVER = 2;  // 待发货
    private static final int STATUS_WAIT_RECEIVE = 3;  // 待收货
    private static final int STATUS_WAIT_COMMENT = 4;  // 待评价
    private static final int STATUS_FINISHED = 5;      // 已完成
    private static final int STATUS_CANCELED = 6;       // 已取消

    // 积分兑换比例：100元 = 1积分
    private static final int POINTS_RATE = 100;

    // ==================== 依赖注入区域 ====================
    // 注入订单相关的所有数据访问层：购物车、地址、用户、商品、订单等
    private final CartItemMapper cartItemMapper;
    private final UserAddressMapper userAddressMapper;
    private final UserAccountMapper userAccountMapper;
    private final ProductMapper productMapper;
    private final ProductSkuMapper productSkuMapper;
    private final ShopMapper shopMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderLogisticsMapper orderLogisticsMapper;
    private final UserCouponMapper userCouponMapper;
    private final CouponMapper couponMapper;
    private final ObjectMapper objectMapper;

    public OrderServiceImpl(CartItemMapper cartItemMapper,
                        UserAddressMapper userAddressMapper,
                        UserAccountMapper userAccountMapper,
                        ProductMapper productMapper,
                        ProductSkuMapper productSkuMapper,
                        ShopMapper shopMapper,
                        OrderMapper orderMapper,
                        OrderItemMapper orderItemMapper,
                        OrderLogisticsMapper orderLogisticsMapper,
                        UserCouponMapper userCouponMapper,
                        CouponMapper couponMapper,
                        ObjectMapper objectMapper) {
        this.cartItemMapper = cartItemMapper;
        this.userAddressMapper = userAddressMapper;
        this.userAccountMapper = userAccountMapper;
        this.productMapper = productMapper;
        this.productSkuMapper = productSkuMapper;
        this.shopMapper = shopMapper;
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.orderLogisticsMapper = orderLogisticsMapper;
        this.userCouponMapper = userCouponMapper;
        this.couponMapper = couponMapper;
        this.objectMapper = objectMapper;
    }

    // ==================== 订单确认核心逻辑 ====================
    // 用户点击"去结算"时调用，计算订单总金额、优惠、运费等
    @Override
    public OrderConfirmResponse confirm(long userId, OrderConfirmRequest req) {
        // 步骤1: 加载用户购物车中的有效商品
        List<CartItem> cartItems = loadValidCartItems(userId, req.getCartIds());

        long shopId = singleShopId(cartItems);
        Shop shop = shopMapper.selectById(shopId);
        if (shop == null) {
            throw BizException.notFound("店铺不存在");
        }

        Map<Long, Product> productMap = loadProducts(cartItems);
        Map<Long, ProductSku> skuMap = loadSkus(cartItems);

        List<OrderConfirmResponse.ProductItem> products = new ArrayList<>();
        BigDecimal shopTotal = BigDecimal.ZERO;

        for (CartItem c : cartItems) {
            Product p = productMap.get(c.getProductId());
            ProductSku sku = skuMap.get(c.getSkuId());
            if (!isValid(p, sku, c)) {
                throw BizException.badRequest("购物车存在失效商品");
            }

            BigDecimal price = sku.getPrice();
            int qty = c.getQuantity();
            BigDecimal subtotal = price.multiply(BigDecimal.valueOf(qty));
            shopTotal = shopTotal.add(subtotal);

            products.add(new OrderConfirmResponse.ProductItem(
                    c.getProductId(),
                    c.getSkuId(),
                    p.getProductName(),
                    sku.getImage() != null ? sku.getImage() : p.getImage(),
                    price,
                    specsText(sku.getSpecsJson()),
                    qty,
                    subtotal
            ));
        }

        BigDecimal shippingFee = BigDecimal.ZERO;
        BigDecimal couponDiscount = BigDecimal.ZERO;
        BigDecimal pointsDiscount = BigDecimal.ZERO;
        BigDecimal actualAmount = shopTotal.add(shippingFee).subtract(couponDiscount).subtract(pointsDiscount);

        OrderConfirmResponse.Address address = defaultAddress(userId);

        List<OrderConfirmResponse.ShopOrderItems> orderItems = List.of(new OrderConfirmResponse.ShopOrderItems(
                shopId,
                shop.getShopName(),
                products,
                shippingFee,
                shopTotal
        ));

        List<OrderConfirmResponse.AvailableCoupon> availableCoupons = availableCoupons(userId, shopTotal);

        UserAccount user = userAccountMapper.selectById(userId);
        int availablePoints = user == null || user.getPoints() == null ? 0 : user.getPoints();

        return new OrderConfirmResponse(
                address,
                orderItems,
                availableCoupons,
                shopTotal,
                shippingFee,
                couponDiscount,
                pointsDiscount,
                actualAmount,
                availablePoints,
                POINTS_RATE
        );
    }

    @Override
    public OrderCreateResponse create(long userId, OrderCreateRequest req) {
        List<CartItem> cartItems = loadValidCartItems(userId, req.getCartIds());

        long shopId = singleShopId(cartItems);
        Shop shop = shopMapper.selectById(shopId);
        if (shop == null) {
            throw BizException.notFound("店铺不存在");
        }

        UserAddress address = userAddressMapper.selectOne(new LambdaQueryWrapper<UserAddress>()
                .eq(UserAddress::getUserId, userId)
                .eq(UserAddress::getAddressId, req.getAddressId())
                .last("limit 1"));
        if (address == null) {
            throw BizException.badRequest("收货地址不存在");
        }

        Map<Long, Product> productMap = loadProducts(cartItems);
        Map<Long, ProductSku> skuMap = loadSkus(cartItems);

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem c : cartItems) {
            Product p = productMap.get(c.getProductId());
            ProductSku sku = skuMap.get(c.getSkuId());
            if (!isValid(p, sku, c)) {
                throw BizException.badRequest("购物车存在失效商品");
            }

            BigDecimal price = sku.getPrice();
            int qty = c.getQuantity();
            BigDecimal subtotal = price.multiply(BigDecimal.valueOf(qty));
            totalAmount = totalAmount.add(subtotal);

            OrderItem oi = new OrderItem();
            oi.setProductId(p.getProductId());
            oi.setSkuId(sku.getSkuId());
            oi.setProductName(p.getProductName());
            oi.setImage(sku.getImage() != null ? sku.getImage() : p.getImage());
            oi.setPrice(price);
            oi.setCostPrice(p.getCostPrice());
            oi.setSpecs(specsText(sku.getSpecsJson()));
            oi.setQuantity(qty);
            oi.setSubtotal(subtotal);
            oi.setCanComment(0);
            orderItems.add(oi);
        }

        BigDecimal shippingFee = BigDecimal.ZERO;
        BigDecimal couponDiscount = computeCouponDiscount(userId, req.getCouponId(), totalAmount);
        BigDecimal pointsDiscount = computePointsDiscount(userId, req.getUsePoints());

        BigDecimal actualAmount = totalAmount.add(shippingFee).subtract(couponDiscount).subtract(pointsDiscount);
        if (actualAmount.compareTo(BigDecimal.ZERO) < 0) {
            actualAmount = BigDecimal.ZERO;
        }

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setShopId(shopId);
        order.setStatus(STATUS_WAIT_PAY);
        order.setTotalAmount(totalAmount);
        order.setShippingFee(shippingFee);
        order.setCouponDiscount(couponDiscount);
        order.setPointsDiscount(pointsDiscount);
        order.setActualAmount(actualAmount);
        order.setPaymentMethod(req.getPaymentMethod());
        order.setRemark(req.getRemark());

        order.setReceiverName(address.getReceiverName());
        order.setReceiverPhone(address.getReceiverPhone());
        order.setProvince(address.getProvince());
        order.setCity(address.getCity());
        order.setDistrict(address.getDistrict());
        order.setAddressDetail(address.getDetail());

        orderMapper.insert(order);

        for (OrderItem oi : orderItems) {
            oi.setOrderId(order.getOrderId());
            orderItemMapper.insert(oi);
        }

        if (req.getCouponId() != null && couponDiscount.compareTo(BigDecimal.ZERO) > 0) {
            markCouponUsed(userId, req.getCouponId(), order.getOrderId());
        }

        if (req.getUsePoints() != null && req.getUsePoints() > 0 && pointsDiscount.compareTo(BigDecimal.ZERO) > 0) {
            consumePoints(userId, req.getUsePoints());
        }

        cartItemMapper.delete(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .in(CartItem::getCartId, req.getCartIds()));

        return new OrderCreateResponse(order.getOrderId(), order.getOrderNo(), order.getActualAmount(), paymentUrl(order.getOrderId(), req.getPaymentMethod()));
    }

/**
 * 根据用户ID、页码、每页大小、订单状态和关键词查询订单列表
 * @param userId 用户ID
 * @param page 页码
 * @param pageSize 每页大小
 * @param status 订单状态，可为null
 * @param keyword 搜索关键词，可为null
 * @return 分页响应对象，包含订单列表和总记录数
 */
    @Override
    public PageResponse<OrderListResponse.OrderItem> list(long userId, int page, int pageSize, Integer status, String keyword) {
    // 创建查询条件包装器，设置用户ID为查询条件
        LambdaQueryWrapper<Order> qw = new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId);

    // 如果订单状态不为空且不为0，则添加状态查询条件
        if (status != null && status != 0) {
            qw.eq(Order::getStatus, status);
        }

    // 如果关键词不为空且不是空白字符串，则添加关键词搜索条件
        if (keyword != null && !keyword.isBlank()) {
            final String kw = keyword;
        // 存储根据商品名称搜索到的订单ID集合
            final Set<Long> orderIdsByKeyword = new HashSet<>();
        // 查询商品名称包含关键词的订单项，收集其订单ID
            for (OrderItem oi : orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>().like(OrderItem::getProductName, kw))) {
                orderIdsByKeyword.add(oi.getOrderId());
            }

        // 如果根据商品名称没有找到匹配的订单，则只按订单号搜索
            if (orderIdsByKeyword.isEmpty()) {
                qw.like(Order::getOrderNo, kw);
            } else {
            // 如果找到了匹配的订单，则按订单号或订单ID搜索
                qw.and(w -> w.like(Order::getOrderNo, kw)
                        .or()
                        .in(Order::getOrderId, orderIdsByKeyword));
            }
        }

    // 按创建时间降序排序
        qw.orderByDesc(Order::getCreateTime);

    // 创建分页对象
        Page<Order> p = new Page<>(page, pageSize);
    // 执行分页查询
        Page<Order> result = orderMapper.selectPage(p, qw);

    // 如果查询结果为空，返回空列表
        if (result.getRecords().isEmpty()) {
            return new PageResponse<>(0, List.of());
        }

    // 收集订单ID和店铺ID集合
        Set<Long> orderIds = new HashSet<>();
        Set<Long> shopIds = new HashSet<>();
        for (Order o : result.getRecords()) {
            orderIds.add(o.getOrderId());
            shopIds.add(o.getShopId());
        }

    // 将订单项按订单ID分组
        Map<Long, List<OrderItem>> itemsMap = new HashMap<>();
        for (OrderItem oi : orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>().in(OrderItem::getOrderId, orderIds))) {
            itemsMap.computeIfAbsent(oi.getOrderId(), k -> new ArrayList<>()).add(oi);
        }

    // 批量查询店铺信息并按店铺ID分组
        Map<Long, Shop> shopMap = new HashMap<>();
        for (Shop s : shopMapper.selectBatchIds(shopIds)) {
            shopMap.put(s.getShopId(), s);
        }

    // 将查询结果转换为响应对象
        List<OrderListResponse.OrderItem> list = result.getRecords().stream().map(o -> {
        // 获取订单项列表
            List<OrderListResponse.Item> items = itemsMap.getOrDefault(o.getOrderId(), List.of()).stream().map(oi -> new OrderListResponse.Item(
                    oi.getOrderItemId(),
                    oi.getProductId(),
                    oi.getSkuId(),
                    oi.getProductName(),
                    oi.getImage(),
                    oi.getPrice(),
                    oi.getSpecs(),
                    oi.getQuantity(),
                    oi.getSubtotal(),
                    oi.getCanComment() != null && oi.getCanComment() == 1
            )).toList();

        // 获取订单按钮列表
            List<String> buttons = listButtons(o.getStatus());

        // 获取店铺信息
            Shop shop = shopMap.get(o.getShopId());
        // 构建订单响应对象
            return new OrderListResponse.OrderItem(
                    o.getOrderId(),
                    o.getOrderNo(),
                    o.getStatus(),
                    statusText(o.getStatus()),
                    o.getShopId(),
                    shop == null ? null : shop.getShopName(),
                    o.getTotalAmount(),
                    o.getShippingFee(),
                    o.getActualAmount(),
                    o.getPaymentMethod(),
                    o.getCreateTime() == null ? null : o.getCreateTime().format(DATETIME),
                    o.getPayTime() == null ? null : o.getPayTime().format(DATETIME),
                    items,
                    buttons
            );
        }).toList();

    // 返回分页响应对象
        return new PageResponse<>(result.getTotal(), list);
    }

    @Override
    public OrderListResponse.StatusCount statusCount(long userId) {
        long waitPay = orderMapper.selectCount(new LambdaQueryWrapper<Order>().eq(Order::getUserId, userId).eq(Order::getStatus, STATUS_WAIT_PAY));
        long waitDeliver = orderMapper.selectCount(new LambdaQueryWrapper<Order>().eq(Order::getUserId, userId).eq(Order::getStatus, STATUS_WAIT_DELIVER));
        long waitReceive = orderMapper.selectCount(new LambdaQueryWrapper<Order>().eq(Order::getUserId, userId).eq(Order::getStatus, STATUS_WAIT_RECEIVE));
        long waitComment = orderMapper.selectCount(new LambdaQueryWrapper<Order>().eq(Order::getUserId, userId).eq(Order::getStatus, STATUS_WAIT_COMMENT));
        return new OrderListResponse.StatusCount(waitPay, waitDeliver, waitReceive, waitComment);
    }

    @Override
    public OrderDetailResponse detail(long userId, long orderId) {
        Order order = orderMapper.selectOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getOrderId, orderId)
                .eq(Order::getUserId, userId)
                .last("limit 1"));
        if (order == null) {
            throw BizException.notFound("订单不存在");
        }

        Shop shop = shopMapper.selectById(order.getShopId());

        List<OrderItem> orderItems = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, orderId)
                .orderByAsc(OrderItem::getOrderItemId));

        List<OrderDetailResponse.Item> items = orderItems.stream().map(oi -> new OrderDetailResponse.Item(
                oi.getOrderItemId(),
                oi.getProductId(),
                oi.getSkuId(),
                oi.getProductName(),
                oi.getImage(),
                oi.getPrice(),
                oi.getSpecs(),
                oi.getQuantity(),
                oi.getSubtotal(),
                oi.getCanComment() != null && oi.getCanComment() == 1,
                false
        )).toList();

        OrderLogistics logistics = orderLogisticsMapper.selectOne(new LambdaQueryWrapper<OrderLogistics>()
                .eq(OrderLogistics::getOrderId, orderId)
                .last("limit 1"));

        OrderDetailResponse.Logistics logisticsVo = null;
        if (logistics != null) {
            logisticsVo = new OrderDetailResponse.Logistics(
                    logistics.getExpressCompany(),
                    logistics.getExpressNo(),
                    logistics.getCurrentStatus(),
                    parseTraces(logistics.getTracesJson())
            );
        }

        OrderDetailResponse.Address address = new OrderDetailResponse.Address(
                order.getReceiverName(),
                order.getReceiverPhone(),
                order.getProvince(),
                order.getCity(),
                order.getDistrict(),
                order.getAddressDetail()
        );

        return new OrderDetailResponse(
                order.getOrderId(),
                order.getOrderNo(),
                order.getStatus(),
                statusText(order.getStatus()),
                order.getShopId(),
                shop == null ? null : shop.getShopName(),
                shop == null ? null : shop.getContactPhone(),
                order.getTotalAmount(),
                order.getShippingFee(),
                order.getCouponDiscount(),
                order.getPointsDiscount(),
                order.getActualAmount(),
                order.getPaymentMethod(),
                paymentMethodText(order.getPaymentMethod()),
                order.getRemark(),
                order.getCreateTime() == null ? null : order.getCreateTime().format(DATETIME),
                order.getPayTime() == null ? null : order.getPayTime().format(DATETIME),
                order.getDeliverTime() == null ? null : order.getDeliverTime().format(DATETIME),
                order.getReceiveTime() == null ? null : order.getReceiveTime().format(DATETIME),
                order.getFinishTime() == null ? null : order.getFinishTime().format(DATETIME),
                order.getCancelTime() == null ? null : order.getCancelTime().format(DATETIME),
                address,
                logisticsVo,
                items,
                listButtons(order.getStatus())
        );
    }

    @Override
    public void cancel(long userId, long orderId, OrderCancelRequest req) {
        Order order = mustOwnOrder(userId, orderId);
        if (order.getStatus() != null && order.getStatus() != STATUS_WAIT_PAY) {
            throw BizException.badRequest("当前订单不可取消");
        }

        orderMapper.update(null, new LambdaUpdateWrapper<Order>()
                .eq(Order::getOrderId, orderId)
                .eq(Order::getUserId, userId)
                .set(Order::getStatus, STATUS_CANCELED)
                .set(Order::getCancelTime, LocalDateTime.now())
                .set(Order::getRemark, req.getReason()));
    }

    @Override
    public void confirmReceive(long userId, long orderId) {
        Order order = mustOwnOrder(userId, orderId);
        if (order.getStatus() == null || order.getStatus() != STATUS_WAIT_RECEIVE) {
            throw BizException.badRequest("当前订单不可确认收货");
        }

        orderMapper.update(null, new LambdaUpdateWrapper<Order>()
                .eq(Order::getOrderId, orderId)
                .eq(Order::getUserId, userId)
                .set(Order::getStatus, STATUS_WAIT_COMMENT)
                .set(Order::getReceiveTime, LocalDateTime.now()));

        orderItemMapper.update(null, new LambdaUpdateWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, orderId)
                .set(OrderItem::getCanComment, 1));
    }

    @Override
    public void delete(long userId, long orderId) {
        Order order = mustOwnOrder(userId, orderId);
        if (order.getStatus() == null || (order.getStatus() != STATUS_FINISHED && order.getStatus() != STATUS_CANCELED)) {
            throw BizException.badRequest("当前订单不可删除");
        }
        orderItemMapper.delete(new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));
        orderLogisticsMapper.delete(new LambdaQueryWrapper<OrderLogistics>().eq(OrderLogistics::getOrderId, orderId));
        orderMapper.deleteById(orderId);
    }

    @Transactional
    @Override
    public OrderPayResponse pay(long userId, OrderPayRequest req) {
        Order order = mustOwnOrder(userId, req.getOrderId());
        if (order.getStatus() == null || order.getStatus() != STATUS_WAIT_PAY) {
            throw BizException.badRequest("当前订单不可支付");
        }

        List<OrderItem> items = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, order.getOrderId()));
        if (items.isEmpty()) {
            throw BizException.badRequest("订单商品为空");
        }

        Map<Long, Integer> productQtyMap = new HashMap<>();
        long totalQty = 0;

        for (OrderItem oi : items) {
            int qty = oi.getQuantity() == null ? 0 : oi.getQuantity();
            if (qty <= 0) {
                continue;
            }
            totalQty += qty;
            if (oi.getProductId() != null) {
                productQtyMap.merge(oi.getProductId(), qty, Integer::sum);
            }

            ProductSku sku = productSkuMapper.selectById(oi.getSkuId());
            if (sku == null) {
                throw BizException.badRequest("SKU不存在");
            }
            int stock = sku.getStock() == null ? 0 : sku.getStock();
            if (stock < qty) {
                throw BizException.badRequest("库存不足");
            }
            sku.setStock(stock - qty);
            productSkuMapper.updateById(sku);
        }

        // 更新商品销量与商品库存（按 SKU 汇总）
        for (Map.Entry<Long, Integer> e : productQtyMap.entrySet()) {
            Long productId = e.getKey();
            int qty = e.getValue() == null ? 0 : e.getValue();
            if (productId == null || qty <= 0) {
                continue;
            }

            Product p = productMapper.selectById(productId);
            if (p == null) {
                continue;
            }

            long oldSales = p.getSales() == null ? 0 : p.getSales();
            long newSales = oldSales + qty;

            int sumStock = productSkuMapper.selectList(new LambdaQueryWrapper<ProductSku>()
                            .eq(ProductSku::getProductId, productId))
                    .stream()
                    .map(ProductSku::getStock)
                    .filter(Objects::nonNull)
                    .mapToInt(Integer::intValue)
                    .sum();

            productMapper.update(null, new LambdaUpdateWrapper<Product>()
                    .eq(Product::getProductId, productId)
                    .set(Product::getSales, newSales)
                    .set(Product::getStock, sumStock));
        }

        // 更新店铺累计成交（用于商家侧统计展示）
        Shop shop = shopMapper.selectById(order.getShopId());
        if (shop != null) {
            long oldCount = shop.getTotalSalesCount() == null ? 0 : shop.getTotalSalesCount();
            BigDecimal oldAmount = shop.getTotalSalesAmount() == null ? BigDecimal.ZERO : shop.getTotalSalesAmount();
            BigDecimal addAmount = order.getActualAmount() == null ? BigDecimal.ZERO : order.getActualAmount();

            shopMapper.update(null, new LambdaUpdateWrapper<Shop>()
                    .eq(Shop::getShopId, shop.getShopId())
                    .set(Shop::getTotalSalesCount, oldCount + totalQty)
                    .set(Shop::getTotalSalesAmount, oldAmount.add(addAmount)));
        }

        orderMapper.update(null, new LambdaUpdateWrapper<Order>()
                .eq(Order::getOrderId, order.getOrderId())
                .eq(Order::getUserId, userId)
                .set(Order::getStatus, STATUS_WAIT_DELIVER)
                .set(Order::getPaymentMethod, req.getPaymentMethod())
                .set(Order::getPayTime, LocalDateTime.now()));

        return new OrderPayResponse(paymentUrl(order.getOrderId(), req.getPaymentMethod()), null);
    }

    @Override
    public Map<String, Object> payStatus(long userId, long orderId) {
        Order order = mustOwnOrder(userId, orderId);
        Map<String, Object> res = new HashMap<>();
        res.put("orderId", order.getOrderId());
        res.put("status", order.getStatus());
        res.put("statusText", statusText(order.getStatus()));
        res.put("paid", order.getStatus() != null && order.getStatus() != STATUS_WAIT_PAY);
        return res;
    }

    @Override
    public OrderDetailResponse.Logistics logistics(long userId, long orderId) {
        mustOwnOrder(userId, orderId);
        OrderLogistics logistics = orderLogisticsMapper.selectOne(new LambdaQueryWrapper<OrderLogistics>()
                .eq(OrderLogistics::getOrderId, orderId)
                .last("limit 1"));
        if (logistics == null) {
            return new OrderDetailResponse.Logistics(null, null, null, List.of());
        }
        return new OrderDetailResponse.Logistics(
                logistics.getExpressCompany(),
                logistics.getExpressNo(),
                logistics.getCurrentStatus(),
                parseTraces(logistics.getTracesJson())
        );
    }

    @Override
    public void remindDeliver(long userId, long orderId) {
        mustOwnOrder(userId, orderId);
    }

    private List<CartItem> loadValidCartItems(long userId, List<Long> cartIds) {
        List<CartItem> cartItems = cartItemMapper.selectList(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .in(CartItem::getCartId, cartIds));
        if (cartItems.isEmpty()) {
            throw BizException.badRequest("购物车为空");
        }
        for (CartItem c : cartItems) {
            if (c.getIsValid() == null || c.getIsValid() != 1) {
                throw BizException.badRequest("购物车存在失效商品");
            }
        }
        return cartItems;
    }

    private long singleShopId(List<CartItem> cartItems) {
        Set<Long> shopIds = new HashSet<>();
        for (CartItem c : cartItems) {
            shopIds.add(c.getShopId());
        }
        if (shopIds.size() != 1) {
            throw BizException.badRequest("一次下单仅支持同一家店铺商品");
        }
        return shopIds.iterator().next();
    }

    private Map<Long, Product> loadProducts(List<CartItem> cartItems) {
        Set<Long> productIds = new HashSet<>();
        for (CartItem c : cartItems) {
            productIds.add(c.getProductId());
        }
        Map<Long, Product> map = new HashMap<>();
        for (Product p : productMapper.selectBatchIds(productIds)) {
            map.put(p.getProductId(), p);
        }
        return map;
    }

    private Map<Long, ProductSku> loadSkus(List<CartItem> cartItems) {
        Set<Long> skuIds = new HashSet<>();
        for (CartItem c : cartItems) {
            skuIds.add(c.getSkuId());
        }
        Map<Long, ProductSku> map = new HashMap<>();
        for (ProductSku sku : productSkuMapper.selectBatchIds(skuIds)) {
            map.put(sku.getSkuId(), sku);
        }
        return map;
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

    private OrderConfirmResponse.Address defaultAddress(long userId) {
        UserAddress addr = userAddressMapper.selectOne(new LambdaQueryWrapper<UserAddress>()
                .eq(UserAddress::getUserId, userId)
                .eq(UserAddress::getIsDefault, 1)
                .last("limit 1"));
        if (addr == null) {
            addr = userAddressMapper.selectOne(new LambdaQueryWrapper<UserAddress>()
                    .eq(UserAddress::getUserId, userId)
                    .orderByDesc(UserAddress::getUpdateTime)
                    .orderByDesc(UserAddress::getCreateTime)
                    .last("limit 1"));
        }
        if (addr == null) {
            return null;
        }
        return new OrderConfirmResponse.Address(
                addr.getAddressId(),
                addr.getReceiverName(),
                addr.getReceiverPhone(),
                addr.getProvince(),
                addr.getCity(),
                addr.getDistrict(),
                addr.getDetail(),
                addr.getIsDefault() != null && addr.getIsDefault() == 1
        );
    }

    private List<OrderConfirmResponse.AvailableCoupon> availableCoupons(long userId, BigDecimal totalAmount) {
        List<UserCoupon> ucs = userCouponMapper.selectList(new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getStatus, 0)
                .orderByDesc(UserCoupon::getReceiveTime)
                .last("limit 50"));
        if (ucs.isEmpty()) {
            return List.of();
        }

        Set<Long> couponIds = new HashSet<>();
        for (UserCoupon uc : ucs) {
            couponIds.add(uc.getCouponId());
        }

        Map<Long, Coupon> couponMap = new HashMap<>();
        for (Coupon c : couponMapper.selectBatchIds(couponIds)) {
            couponMap.put(c.getCouponId(), c);
        }

        List<OrderConfirmResponse.AvailableCoupon> list = new ArrayList<>();
        for (UserCoupon uc : ucs) {
            Coupon c = couponMap.get(uc.getCouponId());
            if (c == null) {
                continue;
            }
            if (c.getStatus() == null || c.getStatus() != 1) {
                continue;
            }
            if (c.getMinAmount() != null && totalAmount.compareTo(c.getMinAmount()) < 0) {
                continue;
            }
            BigDecimal discount = c.getDiscountValue() == null ? BigDecimal.ZERO : c.getDiscountValue();
            list.add(new OrderConfirmResponse.AvailableCoupon(c.getCouponId(), c.getCouponName(), discount, c.getMinAmount()));
        }

        return list;
    }

    private BigDecimal computeCouponDiscount(long userId, Long couponId, BigDecimal totalAmount) {
        if (couponId == null) {
            return BigDecimal.ZERO;
        }
        UserCoupon uc = userCouponMapper.selectOne(new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getCouponId, couponId)
                .eq(UserCoupon::getStatus, 0)
                .orderByDesc(UserCoupon::getReceiveTime)
                .last("limit 1"));
        if (uc == null) {
            return BigDecimal.ZERO;
        }
        Coupon c = couponMapper.selectById(couponId);
        if (c == null || c.getStatus() == null || c.getStatus() != 1) {
            return BigDecimal.ZERO;
        }
        if (c.getMinAmount() != null && totalAmount.compareTo(c.getMinAmount()) < 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal discount = c.getDiscountValue() == null ? BigDecimal.ZERO : c.getDiscountValue();
        if (discount.compareTo(totalAmount) > 0) {
            discount = totalAmount;
        }
        return discount;
    }

    private void markCouponUsed(long userId, long couponId, long orderId) {
        userCouponMapper.update(null, new LambdaUpdateWrapper<UserCoupon>()
                .eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getCouponId, couponId)
                .eq(UserCoupon::getStatus, 0)
                .set(UserCoupon::getStatus, 1)
                .set(UserCoupon::getOrderId, orderId)
                .set(UserCoupon::getUseTime, LocalDateTime.now()));
    }

    private BigDecimal computePointsDiscount(long userId, Integer usePoints) {
        if (usePoints == null || usePoints <= 0) {
            return BigDecimal.ZERO;
        }
        UserAccount user = userAccountMapper.selectById(userId);
        if (user == null || user.getPoints() == null || user.getPoints() <= 0) {
            return BigDecimal.ZERO;
        }
        int canUse = Math.min(usePoints, user.getPoints());
        return BigDecimal.valueOf(canUse).divide(BigDecimal.valueOf(POINTS_RATE));
    }

    private void consumePoints(long userId, int usePoints) {
        UserAccount user = userAccountMapper.selectById(userId);
        if (user == null || user.getPoints() == null) {
            return;
        }
        int newPoints = Math.max(0, user.getPoints() - usePoints);
        user.setPoints(newPoints);
        userAccountMapper.updateById(user);
    }

    private Order mustOwnOrder(long userId, long orderId) {
        Order order = orderMapper.selectOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getOrderId, orderId)
                .eq(Order::getUserId, userId)
                .last("limit 1"));
        if (order == null) {
            throw BizException.notFound("订单不存在");
        }
        return order;
    }

    private String statusText(Integer status) {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case STATUS_WAIT_PAY -> "待付款";
            case STATUS_WAIT_DELIVER -> "待发货";
            case STATUS_WAIT_RECEIVE -> "待收货";
            case STATUS_WAIT_COMMENT -> "待评价";
            case STATUS_FINISHED -> "已完成";
            case STATUS_CANCELED -> "已取消";
            default -> "未知";
        };
    }

    private String paymentMethodText(String method) {
        if (method == null) {
            return null;
        }
        return switch (method) {
            case "alipay" -> "支付宝";
            case "wechat" -> "微信";
            case "balance" -> "余额";
            default -> method;
        };
    }

    private List<String> listButtons(Integer status) {
        if (status == null) {
            return List.of();
        }
        return switch (status) {
            case STATUS_WAIT_PAY -> List.of("取消订单", "去支付");
            case STATUS_WAIT_DELIVER -> List.of("提醒发货");
            case STATUS_WAIT_RECEIVE -> List.of("确认收货", "查看物流", "联系商家");
            case STATUS_WAIT_COMMENT -> List.of("去评价");
            default -> List.of();
        };
    }

    private String generateOrderNo() {
        String t = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int r = new Random().nextInt(900) + 100;
        return "O" + t + r;
    }

    private String paymentUrl(long orderId, String method) {
        if (method == null) {
            method = "alipay";
        }
        return method + "://pay?orderId=" + orderId;
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

    private List<OrderDetailResponse.Trace> parseTraces(String tracesJson) {
        if (tracesJson == null || tracesJson.isBlank()) {
            return List.of();
        }
        try {
            List<Map<String, Object>> list = objectMapper.readValue(tracesJson, new TypeReference<List<Map<String, Object>>>() {
            });
            List<OrderDetailResponse.Trace> traces = new ArrayList<>();
            for (Map<String, Object> row : list) {
                Object time = row.get("time");
                Object status = row.get("status");
                traces.add(new OrderDetailResponse.Trace(time == null ? null : String.valueOf(time), status == null ? null : String.valueOf(status)));
            }
            return traces;
        } catch (Exception e) {
            return List.of();
        }
    }

    // ==================== 支付成功处理核心逻辑 ====================
    @Override
    @Transactional
    public boolean handlePaymentSuccess(long orderId, String tradeNo, BigDecimal amount, String payMethod) {
        try {
            // 步骤1: 查询订单信息
            Order order = orderMapper.selectById(orderId);
            if (order == null) {
                log.error("订单不存在: {}", orderId);
                return false;
            }

            // 步骤2: 验证订单状态
            if (order.getStatus() != STATUS_WAIT_PAY) {
                log.warn("订单状态不正确，当前状态: {}, 订单ID: {}", order.getStatus(), orderId);
                return false;
            }

            // 步骤3: 验证支付金额
            if (order.getTotalAmount().compareTo(amount) != 0) {
                log.error("支付金额不匹配，订单金额: {}, 支付金额: {}, 订单ID: {}", 
                    order.getTotalAmount(), amount, orderId);
                return false;
            }

            // 步骤4: 更新订单状态
            Order updateOrder = new Order();
            updateOrder.setOrderId(orderId);
            updateOrder.setStatus(STATUS_WAIT_DELIVER);  // 待发货
            updateOrder.setPayMethod(payMethod);         // 支付方式
            updateOrder.setPayTime(LocalDateTime.now()); // 支付时间
            updateOrder.setTradeNo(tradeNo);             // 第三方交易号
            
            int result = orderMapper.updateById(updateOrder);
            if (result <= 0) {
                log.error("更新订单状态失败，订单ID: {}", orderId);
                return false;
            }

            // 步骤5: 扣减商品库存
            boolean stockResult = reduceProductStock(order);
            if (!stockResult) {
                log.error("扣减商品库存失败，订单ID: {}", orderId);
                // 这里可以考虑回滚订单状态，但为了简化，先记录日志
                return false;
            }

            log.info("订单支付处理成功，订单ID: {}, 交易号: {}, 支付方式: {}", 
                orderId, tradeNo, payMethod);
            return true;

        } catch (Exception e) {
            log.error("处理支付成功回调异常，订单ID: " + orderId, e);
            return false;
        }
    }

    /**
     * 扣减商品库存
     */
    private boolean reduceProductStock(Order order) {
        try {
            // 查询订单商品
            List<OrderItem> orderItems = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>()
                    .eq(OrderItem::getOrderId, order.getOrderId())
            );

            for (OrderItem item : orderItems) {
                // 扣减SKU库存
                if (item.getSkuId() != null) {
                    int updateResult = productSkuMapper.update(null,
                        new LambdaUpdateWrapper<ProductSku>()
                            .setSql("stock = stock - " + item.getQuantity())
                            .eq(ProductSku::getSkuId, item.getSkuId())
                            .ge(ProductSku::getStock, item.getQuantity())
                    );
                    if (updateResult <= 0) {
                        log.error("扣减SKU库存失败，SKU ID: {}, 数量: {}", item.getSkuId(), item.getQuantity());
                        return false;
                    }
                }

                // 扣减商品总库存
                int updateResult = productMapper.update(null,
                    new LambdaUpdateWrapper<Product>()
                        .setSql("stock = stock - " + item.getQuantity())
                        .eq(Product::getProductId, item.getProductId())
                        .ge(Product::getStock, item.getQuantity())
                );
                if (updateResult <= 0) {
                    log.error("扣减商品库存失败，商品ID: {}, 数量: {}", item.getProductId(), item.getQuantity());
                    return false;
                }

                // 增加商品销量
                productMapper.update(null,
                    new LambdaUpdateWrapper<Product>()
                        .setSql("sales = sales + " + item.getQuantity())
                        .eq(Product::getProductId, item.getProductId())
                );
            }

            return true;

        } catch (Exception e) {
            log.error("扣减商品库存异常，订单ID: " + order.getOrderId(), e);
            return false;
        }
    }
}
