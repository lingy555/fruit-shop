package com.lingnan.fruitshop.service.impl;

import com.lingnan.fruitshop.service.CommentService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lingnan.fruitshop.common.exception.BizException;
import com.lingnan.fruitshop.dto.customer.common.vo.PageResponse;
import com.lingnan.fruitshop.dto.customer.comment.CommentAddRequest;
import com.lingnan.fruitshop.dto.customer.comment.CommentAppendRequest;
import com.lingnan.fruitshop.dto.customer.comment.vo.MyCommentItemResponse;
import com.lingnan.fruitshop.dto.customer.comment.vo.WaitCommentItemResponse;
import com.lingnan.fruitshop.entity.Comment;
import com.lingnan.fruitshop.entity.Order;
import com.lingnan.fruitshop.entity.OrderItem;
import com.lingnan.fruitshop.mapper.CommentMapper;
import com.lingnan.fruitshop.mapper.OrderItemMapper;
import com.lingnan.fruitshop.mapper.OrderMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    private static final DateTimeFormatter DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final CommentMapper commentMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ObjectMapper objectMapper;

    public CommentServiceImpl(CommentMapper commentMapper,
                          OrderMapper orderMapper,
                          OrderItemMapper orderItemMapper,
                          ObjectMapper objectMapper) {
        this.commentMapper = commentMapper;
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public PageResponse<WaitCommentItemResponse> waitList(long userId, int page, int pageSize) {
        Page<OrderItem> p = new Page<>(page, pageSize);
        Page<OrderItem> result = orderItemMapper.selectPage(p, new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getCanComment, 1)
                .orderByDesc(OrderItem::getCreateTime));

        if (result.getRecords().isEmpty()) {
            return new PageResponse<>(0, List.of());
        }

        List<WaitCommentItemResponse> list = result.getRecords().stream()
                .filter(oi -> {
                    Order o = orderMapper.selectById(oi.getOrderId());
                    return o != null && o.getUserId() != null && o.getUserId() == userId;
                })
                .filter(oi -> {
                    Long cnt = commentMapper.selectCount(new LambdaQueryWrapper<Comment>()
                            .eq(Comment::getOrderItemId, oi.getOrderItemId())
                            .eq(Comment::getUserId, userId)
                            .eq(Comment::getStatus, 1));
                    return cnt == null || cnt == 0;
                })
                .map(oi -> new WaitCommentItemResponse(
                        oi.getOrderId(),
                        oi.getOrderItemId(),
                        oi.getProductId(),
                        oi.getSkuId(),
                        oi.getProductName(),
                        oi.getImage(),
                        oi.getPrice(),
                        oi.getSpecs(),
                        oi.getQuantity(),
                        oi.getCreateTime() == null ? null : oi.getCreateTime().format(DATETIME)
                ))
                .toList();

        return new PageResponse<>(result.getTotal(), list);
    }

    @Override
    public void add(long userId, CommentAddRequest req) {
        OrderItem oi = orderItemMapper.selectById(req.getOrderItemId());
        if (oi == null) {
            throw BizException.notFound("订单商品不存在");
        }
        Order order = orderMapper.selectById(oi.getOrderId());
        if (order == null || order.getUserId() == null || order.getUserId() != userId) {
            throw BizException.forbidden("无权限访问");
        }
        if (oi.getCanComment() == null || oi.getCanComment() != 1) {
            throw BizException.badRequest("当前订单商品不可评价");
        }
        if (!req.getProductId().equals(oi.getProductId())) {
            throw BizException.badRequest("商品不匹配");
        }

        Long exists = commentMapper.selectCount(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getOrderItemId, req.getOrderItemId())
                .eq(Comment::getUserId, userId)
                .eq(Comment::getStatus, 1));
        if (exists != null && exists > 0) {
            throw BizException.badRequest("已评价");
        }

        Comment c = new Comment();
        c.setOrderId(order.getOrderId());
        c.setOrderItemId(oi.getOrderItemId());
        c.setUserId(userId);
        c.setProductId(oi.getProductId());
        c.setShopId(order.getShopId());
        c.setScore(req.getScore());
        c.setContent(req.getContent());
        c.setImagesJson(writeJson(req.getImages()));
        c.setSpecs(oi.getSpecs());
        c.setIsAnonymous(Boolean.TRUE.equals(req.getIsAnonymous()) ? 1 : 0);
        c.setStatus(1);
        c.setCreateTime(LocalDateTime.now());
        c.setUpdateTime(LocalDateTime.now());
        commentMapper.insert(c);

        orderItemMapper.update(null, new LambdaUpdateWrapper<OrderItem>()
                .eq(OrderItem::getOrderItemId, oi.getOrderItemId())
                .set(OrderItem::getCanComment, 0));
    }

    @Override
    public void append(long userId, CommentAppendRequest req) {
        Comment c = commentMapper.selectById(req.getCommentId());
        if (c == null || c.getStatus() == null || c.getStatus() != 1) {
            throw BizException.notFound("评价不存在");
        }
        if (c.getUserId() == null || c.getUserId() != userId) {
            throw BizException.forbidden("无权限访问");
        }
        c.setAppendContent(req.getContent());
        c.setAppendImagesJson(writeJson(req.getImages()));
        c.setAppendTime(LocalDateTime.now());
        c.setUpdateTime(LocalDateTime.now());
        commentMapper.updateById(c);
    }

    @Override
    public PageResponse<MyCommentItemResponse> myList(long userId, int page, int pageSize) {
        Page<Comment> p = new Page<>(page, pageSize);
        Page<Comment> result = commentMapper.selectPage(p, new LambdaQueryWrapper<Comment>()
                .eq(Comment::getUserId, userId)
                .eq(Comment::getStatus, 1)
                .orderByDesc(Comment::getCreateTime));

        List<MyCommentItemResponse> list = result.getRecords().stream().map(c -> new MyCommentItemResponse(
                c.getCommentId(),
                c.getOrderId(),
                c.getOrderItemId(),
                c.getProductId(),
                c.getShopId(),
                c.getScore(),
                c.getContent(),
                readJsonArray(c.getImagesJson()),
                c.getSpecs(),
                c.getIsAnonymous() != null && c.getIsAnonymous() == 1,
                c.getCreateTime() == null ? null : c.getCreateTime().format(DATETIME),
                c.getMerchantReplyContent(),
                c.getMerchantReplyTime() == null ? null : c.getMerchantReplyTime().format(DATETIME),
                c.getAppendContent(),
                readJsonArray(c.getAppendImagesJson()),
                c.getAppendTime() == null ? null : c.getAppendTime().format(DATETIME)
        )).toList();

        return new PageResponse<>(result.getTotal(), list);
    }

    @Override
    public void delete(long userId, long commentId) {
        Comment c = commentMapper.selectById(commentId);
        if (c == null || c.getStatus() == null || c.getStatus() != 1) {
            throw BizException.notFound("评价不存在");
        }
        if (c.getUserId() == null || c.getUserId() != userId) {
            throw BizException.forbidden("无权限访问");
        }
        c.setStatus(0);
        c.setUpdateTime(LocalDateTime.now());
        commentMapper.updateById(c);
    }

    private String writeJson(List<String> list) {
        if (list == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            return null;
        }
    }

    private List<String> readJsonArray(String json) {
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
