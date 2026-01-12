package com.lingnan.fruitshop.service;

import com.lingnan.fruitshop.dto.admin.banner.AdminBannerAddRequest;
import com.lingnan.fruitshop.dto.admin.banner.AdminBannerUpdateRequest;
import com.lingnan.fruitshop.dto.admin.banner.vo.AdminBannerItemResponse;
import java.time.LocalDateTime;
import java.util.List;

public interface AdminBannerService {
    List<AdminBannerItemResponse> list();
    void add(AdminBannerAddRequest req);
    void update(long bannerId, AdminBannerUpdateRequest req);
    void delete(long bannerId);
}
