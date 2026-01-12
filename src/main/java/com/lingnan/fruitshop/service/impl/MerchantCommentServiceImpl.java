package com.lingnan.fruitshop.service.impl;

import com.lingnan.fruitshop.service.MerchantCommentService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lingnan.fruitshop.common.exception.BizException;
import com.lingnan.fruitshop.dto.merchant.comment.MerchantCommentReplyRequest;
import com.lingnan.fruitshop.dto.merchant.comment.vo.MerchantCommentListResponse;
import com.lingnan.fruitshop.entity.Comment;
import com.lingnan.fruitshop.entity.Order;
import com.lingnan.fruitshop.entity.Product;
import com.lingnan.fruitshop.entity.UserAccount;
import com.lingnan.fruitshop.mapper.CommentMapper;
import com.lingnan.fruitshop.mapper.OrderMapper;
import com.lingnan.fruitshop.mapper.ProductMapper;
import com.lingnan.fruitshop.mapper.UserAccountMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class MerchantCommentServiceImpl implements MerchantCommentService {

    private static final DateTimeFormatter DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final CommentMapper commentMapper;
    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;
    private final UserAccountMapper userAccountMapper;
    private final ObjectMapper objectMapper;

    public MerchantCommentServiceImpl(CommentMapper commentMapper,
                                 OrderMapper orderMapper,
                                 ProductMapper productMapper,
                                 UserAccountMapper userAccountMapper,
                                 ObjectMapper objectMapper) {
        this.commentMapper = commentMapper;
        this.orderMapper = orderMapper;
        this.productMapper = productMapper;
        this.userAccountMapper = userAccountMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public MerchantCommentListResponse list(long shopId, int page, int pageSize, Long productId, Integer score, Boolean hasReply, String keyword) {
        LambdaQueryWrapper<Comment> qw = new LambdaQueryWrapper<Comment>()
                .eq(Comment::getShopId, shopId)
                .eq(Comment::getStatus, 1);

        if (productId != null) {
            qw.eq(Comment::getProductId, productId);
        }
        if (score != null) {
            qw.eq(Comment::getScore, score);
        }
        if (hasReply != null) {
            if (hasReply) {
                qw.isNotNull(Comment::getMerchantReplyContent).ne(Comment::getMerchantReplyContent, "");
            } else {
                qw.and(w -> w.isNull(Comment::getMerchantReplyContent).or().eq(Comment::getMerchantReplyContent, ""));
            }
        }
        if (keyword != null && !keyword.isBlank()) {
            qw.like(Comment::getContent, keyword);
        }

        qw.orderByDesc(Comment::getCreateTime);

        Page<Comment> p = new Page<>(page, pageSize);
        Page<Comment> result = commentMapper.selectPage(p, qw);

        long totalAll = commentMapper.selectCount(new LambdaQueryWrapper<Comment>().eq(Comment::getShopId, shopId).eq(Comment::getStatus, 1));
        long goodCount = commentMapper.selectCount(new LambdaQueryWrapper<Comment>().eq(Comment::getShopId, shopId).eq(Comment::getStatus, 1).ge(Comment::getScore, 4));
        long unreplyCount = commentMapper.selectCount(new LambdaQueryWrapper<Comment>().eq(Comment::getShopId, shopId).eq(Comment::getStatus, 1)
                .and(w -> w.isNull(Comment::getMerchantReplyContent).or().eq(Comment::getMerchantReplyContent, "")));

        double goodRate = totalAll == 0 ? 0D : (goodCount * 100.0 / totalAll);
        double avgScore = avgScore(shopId);

        MerchantCommentListResponse.Summary summary = new MerchantCommentListResponse.Summary(goodRate, avgScore, unreplyCount);

        if (result.getRecords().isEmpty()) {
            return new MerchantCommentListResponse(0, summary, List.of());
        }

        Set<Long> userIds = new HashSet<>();
        Set<Long> orderIds = new HashSet<>();
        Set<Long> productIds = new HashSet<>();
        for (Comment c : result.getRecords()) {
            userIds.add(c.getUserId());
            orderIds.add(c.getOrderId());
            productIds.add(c.getProductId());
        }

        Map<Long, UserAccount> userMap = new HashMap<>();
        for (UserAccount u : userAccountMapper.selectBatchIds(userIds)) {
            userMap.put(u.getUserId(), u);
        }

        Map<Long, Order> orderMap = new HashMap<>();
        for (Order o : orderMapper.selectBatchIds(orderIds)) {
            orderMap.put(o.getOrderId(), o);
        }

        Map<Long, Product> productMap = new HashMap<>();
        for (Product p0 : productMapper.selectBatchIds(productIds)) {
            productMap.put(p0.getProductId(), p0);
        }

        List<MerchantCommentListResponse.Item> list = result.getRecords().stream().map(c -> {
            UserAccount u = userMap.get(c.getUserId());
            Order o = orderMap.get(c.getOrderId());
            Product p0 = productMap.get(c.getProductId());

            String username = u == null ? "用户" : maskName(u.getNickname() != null ? u.getNickname() : u.getUsername());
            String avatar = u == null ? null : u.getAvatar();
            String orderNo = o == null ? null : o.getOrderNo();

            MerchantCommentListResponse.Reply reply = null;
            boolean replied = c.getMerchantReplyContent() != null && !c.getMerchantReplyContent().isBlank();
            if (replied) {
                reply = new MerchantCommentListResponse.Reply(c.getMerchantReplyContent(), c.getMerchantReplyTime() == null ? null : c.getMerchantReplyTime().format(DATETIME));
            }

            return new MerchantCommentListResponse.Item(
                    c.getCommentId(),
                    c.getOrderId(),
                    orderNo,
                    c.getUserId(),
                    username,
                    avatar,
                    c.getProductId(),
                    p0 == null ? null : p0.getProductName(),
                    p0 == null ? null : p0.getImage(),
                    c.getScore(),
                    c.getContent(),
                    readJsonArray(c.getImagesJson()),
                    c.getSpecs(),
                    c.getCreateTime() == null ? null : c.getCreateTime().format(DATETIME),
                    reply,
                    replied
            );
        }).toList();

        return new MerchantCommentListResponse(result.getTotal(), summary, list);
    }

    @Override
    public void reply(long shopId, MerchantCommentReplyRequest req) {
        Comment c = commentMapper.selectOne(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getShopId, shopId)
                .eq(Comment::getCommentId, req.getCommentId())
                .eq(Comment::getStatus, 1)
                .last("limit 1"));
        if (c == null) {
            throw BizException.notFound("评价不存在");
        }
        commentMapper.update(null, new LambdaUpdateWrapper<Comment>()
                .eq(Comment::getCommentId, c.getCommentId())
                .eq(Comment::getShopId, shopId)
                .set(Comment::getMerchantReplyContent, req.getContent())
                .set(Comment::getMerchantReplyTime, LocalDateTime.now()));
    }

    @Override
    public void deleteReply(long shopId, long commentId) {
        Comment c = commentMapper.selectOne(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getShopId, shopId)
                .eq(Comment::getCommentId, commentId)
                .eq(Comment::getStatus, 1)
                .last("limit 1"));
        if (c == null) {
            throw BizException.notFound("评价不存在");
        }
        commentMapper.update(null, new LambdaUpdateWrapper<Comment>()
                .eq(Comment::getCommentId, c.getCommentId())
                .eq(Comment::getShopId, shopId)
                .set(Comment::getMerchantReplyContent, null)
                .set(Comment::getMerchantReplyTime, null));
    }

    private double avgScore(long shopId) {
        List<Comment> list = commentMapper.selectList(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getShopId, shopId)
                .eq(Comment::getStatus, 1));
        if (list.isEmpty()) {
            return 0D;
        }
        double sum = 0;
        int cnt = 0;
        for (Comment c : list) {
            if (c.getScore() != null) {
                sum += c.getScore();
                cnt++;
            }
        }
        return cnt == 0 ? 0D : (sum / cnt);
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
