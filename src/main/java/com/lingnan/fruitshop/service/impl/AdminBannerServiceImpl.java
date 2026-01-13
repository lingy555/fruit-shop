package com.lingnan.fruitshop.service.impl;

import com.lingnan.fruitshop.service.AdminBannerService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lingnan.fruitshop.common.exception.BizException;
import com.lingnan.fruitshop.dto.admin.banner.AdminBannerAddRequest;
import com.lingnan.fruitshop.dto.admin.banner.AdminBannerUpdateRequest;
import com.lingnan.fruitshop.dto.admin.banner.vo.AdminBannerItemResponse;
import com.lingnan.fruitshop.entity.Banner;
import com.lingnan.fruitshop.mapper.BannerMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 轮播图服务实现类
 * 实现了AdminBannerService接口，提供轮播图的增删改查功能
 */
@Service
public class AdminBannerServiceImpl implements AdminBannerService {

    // 日期时间格式化器，用于格式化日期时间字符串
    private static final DateTimeFormatter DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // 轮播图数据访问层，用于数据库操作
    private final BannerMapper bannerMapper;

    /**
     * 构造函数，通过依赖注入方式获取BannerMapper实例
     * @param bannerMapper 轮播图数据访问层接口
     */
    public AdminBannerServiceImpl(BannerMapper bannerMapper) {
    // 将传入的bannerMapper实例赋值给类的成员变量
        this.bannerMapper = bannerMapper;
    }

    /**
     * 获取轮播图列表
     * @return 轮播图列表响应对象，包含轮播图的所有信息
     */
    @Override
    public List<AdminBannerItemResponse> list() {
        return bannerMapper.selectList(new LambdaQueryWrapper<Banner>().orderByAsc(Banner::getSort))
                .stream()
                .map(b -> new AdminBannerItemResponse(
                        b.getBannerId(),
                        b.getTitle(),
                        b.getImage(),
                        b.getPosition(),
                        b.getLinkType(),
                        b.getLinkValue(),
                        b.getSort(),
                        b.getStatus(),
                        format(b.getStartTime()),
                        format(b.getEndTime()),
                        format(b.getCreateTime())
                ))
                .toList();
    }

    /**
     * 添加轮播图
     * @param req 轮播图添加请求对象，包含要添加的轮播图信息
     */
    @Override
    public void add(AdminBannerAddRequest req) {
        Banner b = new Banner();
        b.setTitle(req.getTitle());
        b.setImage(req.getImage());
        b.setPosition(req.getPosition());
        b.setLinkType(req.getLinkType());
        b.setLinkValue(req.getLinkValue());
        b.setSort(req.getSort());
        b.setStatus(1); // 默认状态为1（启用）
        b.setStartTime(parse(req.getStartTime()));
        b.setEndTime(parse(req.getEndTime()));
        bannerMapper.insert(b);
    }

    /**
     * 更新轮播图信息
     * @param bannerId 轮播图ID
     * @param req 轮播图更新请求对象，包含要更新的轮播图信息
     */
    @Override
    public void update(long bannerId, AdminBannerUpdateRequest req) {
        Banner b = bannerMapper.selectById(bannerId);
        if (b == null) {
            throw BizException.notFound("轮播图不存在");
        }

        b.setTitle(req.getTitle());
        b.setImage(req.getImage());
        b.setPosition(req.getPosition());
        b.setLinkType(req.getLinkType());
        b.setLinkValue(req.getLinkValue());
        b.setSort(req.getSort());
        b.setStatus(req.getStatus());
        b.setStartTime(parse(req.getStartTime()));
        b.setEndTime(parse(req.getEndTime()));
        bannerMapper.updateById(b);
    }

    /**
     * 删除轮播图
     * @param bannerId 轮播图ID
     */
    @Override
    public void delete(long bannerId) {
        bannerMapper.deleteById(bannerId);
    }

    /**
     * 格式化日期时间为字符串
     * @param dt LocalDateTime对象
     * @return 格式化后的日期时间字符串，如果输入为null则返回null
     */
    private String format(LocalDateTime dt) {
        return dt == null ? null : DATETIME.format(dt);
    }

    /**
     * 解析日期时间字符串为LocalDateTime对象
     * @param s 日期时间字符串
     * @return 解析后的LocalDateTime对象，如果解析失败则返回null
     */
    private LocalDateTime parse(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(s, DATETIME);
        } catch (Exception e) {
            return null;
        }
    }
}
