package com.lingnan.fruitshop.service.impl;

import com.lingnan.fruitshop.service.AdminCommentService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lingnan.fruitshop.common.exception.BizException;
import com.lingnan.fruitshop.dto.admin.comment.AdminCommentUpdateStatusRequest;
import com.lingnan.fruitshop.dto.admin.comment.vo.AdminCommentListResponse;
import com.lingnan.fruitshop.entity.*;
import com.lingnan.fruitshop.mapper.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class AdminCommentServiceImpl implements AdminCommentService {

    private static final DateTimeFormatter DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final CommentMapper commentMapper;
    private final UserAccountMapper userAccountMapper;
    private final ProductMapper productMapper;
    private final ShopMapper shopMapper;
    private final ObjectMapper objectMapper;

    public AdminCommentServiceImpl(CommentMapper commentMapper,
                               UserAccountMapper userAccountMapper,
                               ProductMapper productMapper,
                               ShopMapper shopMapper,
                               ObjectMapper objectMapper) {
        this.commentMapper = commentMapper;
        this.userAccountMapper = userAccountMapper;
        this.productMapper = productMapper;
        this.shopMapper = shopMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public AdminCommentListResponse list(int page, int pageSize, Long productId, Long shopId, Integer score, String keyword) {
        LambdaQueryWrapper<Comment> qw = new LambdaQueryWrapper<>();
        if (productId != null) {
            qw.eq(Comment::getProductId, productId);
        }
        if (shopId != null) {
            qw.eq(Comment::getShopId, shopId);
        }
        if (score != null) {
            qw.eq(Comment::getScore, score);
        }
        if (keyword != null && !keyword.isBlank()) {
            qw.like(Comment::getContent, keyword);
        }
        qw.orderByDesc(Comment::getCreateTime);

        Page<Comment> p = new Page<>(page, pageSize);
        Page<Comment> result = commentMapper.selectPage(p, qw);
        if (result.getRecords().isEmpty()) {
            return new AdminCommentListResponse(0, List.of());
        }

        Set<Long> userIds = new HashSet<>();
        Set<Long> productIds = new HashSet<>();
        Set<Long> shopIds = new HashSet<>();
        for (Comment c : result.getRecords()) {
            if (c.getUserId() != null) userIds.add(c.getUserId());
            if (c.getProductId() != null) productIds.add(c.getProductId());
            if (c.getShopId() != null) shopIds.add(c.getShopId());
        }

        Map<Long, UserAccount> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            for (UserAccount u : userAccountMapper.selectBatchIds(userIds)) {
                userMap.put(u.getUserId(), u);
            }
        }

        Map<Long, Product> productMap = new HashMap<>();
        if (!productIds.isEmpty()) {
            for (Product p1 : productMapper.selectBatchIds(productIds)) {
                productMap.put(p1.getProductId(), p1);
            }
        }

        Map<Long, Shop> shopMap = new HashMap<>();
        if (!shopIds.isEmpty()) {
            for (Shop s : shopMapper.selectBatchIds(shopIds)) {
                shopMap.put(s.getShopId(), s);
            }
        }

        List<AdminCommentListResponse.Item> list = result.getRecords().stream().map(c -> {
            UserAccount u = userMap.get(c.getUserId());
            Product p1 = productMap.get(c.getProductId());
            Shop s = shopMap.get(c.getShopId());

            String username;
            if (c.getIsAnonymous() != null && c.getIsAnonymous() == 1) {
                username = "匿名用户";
            } else {
                username = u == null ? "用户" : maskName(u.getNickname() != null ? u.getNickname() : u.getUsername());
            }

            return new AdminCommentListResponse.Item(
                    c.getCommentId(),
                    c.getOrderId(),
                    c.getUserId(),
                    username,
                    c.getProductId(),
                    p1 == null ? null : p1.getProductName(),
                    c.getShopId(),
                    s == null ? null : s.getShopName(),
                    c.getScore(),
                    c.getContent(),
                    readJsonArray(c.getImagesJson()),
                    c.getStatus(),
                    c.getCreateTime() == null ? null : c.getCreateTime().format(DATETIME)
            );
        }).toList();

        return new AdminCommentListResponse(result.getTotal(), list);
    }

    @Override
    public void delete(long commentId) {
        commentMapper.deleteById(commentId);
    }

    @Override
    public void updateStatus(AdminCommentUpdateStatusRequest req) {
        Comment c = commentMapper.selectById(req.getCommentId());
        if (c == null) {
            throw BizException.notFound("评价不存在");
        }
        commentMapper.update(null, new LambdaUpdateWrapper<Comment>()
                .eq(Comment::getCommentId, req.getCommentId())
                .set(Comment::getStatus, req.getStatus())
                .set(Comment::getUpdateTime, LocalDateTime.now()));
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

    private String maskName(String name) {
        if (name == null || name.isBlank()) {
            return "用户";
        }
        if (name.length() <= 1) {
            return name + "***";
        }
        return name.charAt(0) + "***";
    }
}
