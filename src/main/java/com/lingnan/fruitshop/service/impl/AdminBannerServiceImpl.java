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

@Service
public class AdminBannerServiceImpl implements AdminBannerService {

    private static final DateTimeFormatter DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final BannerMapper bannerMapper;

    public AdminBannerServiceImpl(BannerMapper bannerMapper) {
        this.bannerMapper = bannerMapper;
    }

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

    @Override
    public void add(AdminBannerAddRequest req) {
        Banner b = new Banner();
        b.setTitle(req.getTitle());
        b.setImage(req.getImage());
        b.setPosition(req.getPosition());
        b.setLinkType(req.getLinkType());
        b.setLinkValue(req.getLinkValue());
        b.setSort(req.getSort());
        b.setStatus(1);
        b.setStartTime(parse(req.getStartTime()));
        b.setEndTime(parse(req.getEndTime()));
        bannerMapper.insert(b);
    }

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

    @Override
    public void delete(long bannerId) {
        bannerMapper.deleteById(bannerId);
    }

    private String format(LocalDateTime dt) {
        return dt == null ? null : DATETIME.format(dt);
    }

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
