package com.lingnan.fruitshop.service;

import com.lingnan.fruitshop.dto.customer.common.vo.PageResponse;
import com.lingnan.fruitshop.dto.customer.home.vo.HomeIndexResponse;
import com.lingnan.fruitshop.entity.Notice;

import java.util.List;

public interface HomeService {
    HomeIndexResponse index();
    
    List<HomeIndexResponse.BannerItem> banners(String position);
    
    PageResponse<Notice> notices(int page, int pageSize);
}
