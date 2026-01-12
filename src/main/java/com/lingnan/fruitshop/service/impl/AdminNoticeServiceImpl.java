package com.lingnan.fruitshop.service.impl;

import com.lingnan.fruitshop.service.AdminNoticeService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingnan.fruitshop.common.exception.BizException;
import com.lingnan.fruitshop.dto.admin.notice.AdminNoticeAddRequest;
import com.lingnan.fruitshop.dto.admin.notice.AdminNoticeUpdateRequest;
import com.lingnan.fruitshop.dto.admin.notice.vo.AdminNoticeDetailResponse;
import com.lingnan.fruitshop.dto.admin.notice.vo.AdminNoticeListItemResponse;
import com.lingnan.fruitshop.dto.customer.common.vo.PageResponse;
import com.lingnan.fruitshop.entity.Notice;
import com.lingnan.fruitshop.mapper.NoticeMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class AdminNoticeServiceImpl implements AdminNoticeService {

    // ==================== 常量定义 ====================
    private static final DateTimeFormatter DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ==================== 依赖注入区域 ====================
    // 注入公告数据访问层
    private final NoticeMapper noticeMapper;

    public AdminNoticeServiceImpl(NoticeMapper noticeMapper) {
        this.noticeMapper = noticeMapper;
    }

    // ==================== 公告列表查询核心逻辑 ====================
    // 支持按类型和状态筛选，按排序字段和创建时间排序
    @Override
    public PageResponse<AdminNoticeListItemResponse> list(int page, int pageSize, Integer type, Integer status) {
        // 步骤1: 构建查询条件
        LambdaQueryWrapper<Notice> qw = new LambdaQueryWrapper<>();
        if (type != null) {
            qw.eq(Notice::getType, type);           // 按公告类型筛选
        }
        if (status != null) {
            qw.eq(Notice::getStatus, status);       // 按状态筛选
        }
        // 步骤2: 设置排序规则：先按排序字段升序，再按创建时间降序
        qw.orderByAsc(Notice::getSort).orderByDesc(Notice::getCreateTime);

        // 步骤3: 执行分页查询
        Page<Notice> p = new Page<>(page, pageSize);
        Page<Notice> result = noticeMapper.selectPage(p, qw);
        if (result.getRecords().isEmpty()) {
            return new PageResponse<>(0, List.of());
        }

        // 步骤4: 转换为响应对象并格式化时间
        List<AdminNoticeListItemResponse> list = result.getRecords().stream().map(n -> new AdminNoticeListItemResponse(
                n.getNoticeId(),
                n.getTitle(),
                n.getType(),
                n.getStatus(),
                n.getSort(),
                format(n.getCreateTime())                 // 格式化创建时间
        )).toList();

        return new PageResponse<>(result.getTotal(), list);
    }

    // ==================== 公告详情查询核心逻辑 ====================
    @Override
    public AdminNoticeDetailResponse detail(long noticeId) {
        // 步骤1: 根据ID查询公告
        Notice n = noticeMapper.selectById(noticeId);
        if (n == null) {
            throw BizException.notFound("公告不存在");
        }
        
        // 步骤2: 构建详情响应对象
        return new AdminNoticeDetailResponse(
                n.getNoticeId(),
                n.getTitle(),
                n.getContent(),
                n.getType(),
                n.getStatus(),
                n.getSort()
        );
    }

    // ==================== 公告添加核心逻辑 ====================
    @Override
    public void add(AdminNoticeAddRequest req) {
        // 步骤1: 创建公告实体并设置属性
        Notice n = new Notice();
        n.setTitle(req.getTitle());
        n.setContent(req.getContent());
        n.setType(req.getType());
        n.setStatus(req.getStatus());
        n.setSort(req.getSort());
        
        // 步骤2: 保存到数据库
        noticeMapper.insert(n);
    }

    // ==================== 公告更新核心逻辑 ====================
    @Override
    public void update(long noticeId, AdminNoticeUpdateRequest req) {
        // 步骤1: 查询现有公告
        Notice n = noticeMapper.selectById(noticeId);
        if (n == null) {
            throw BizException.notFound("公告不存在");
        }
        
        // 步骤2: 更新公告属性
        n.setTitle(req.getTitle());
        n.setContent(req.getContent());
        n.setType(req.getType());
        n.setStatus(req.getStatus());
        n.setSort(req.getSort());
        
        // 步骤3: 保存更新
        noticeMapper.updateById(n);
    }

    // ==================== 公告删除核心逻辑 ====================
    @Override
    public void delete(long noticeId) {
        // 直接根据ID删除公告（物理删除）
        noticeMapper.deleteById(noticeId);
    }

    // ==================== 时间格式化工具方法 ====================
    // 将LocalDateTime格式化为字符串，便于前端显示
    private String format(LocalDateTime dt) {
        return dt == null ? null : DATETIME.format(dt);
    }
}
