package com.lingnan.fruitshop.controller.customer;

import com.lingnan.fruitshop.common.api.ApiResponse;
import com.lingnan.fruitshop.dto.customer.common.vo.PageResponse;
import com.lingnan.fruitshop.dto.customer.home.vo.HomeIndexResponse;
import com.lingnan.fruitshop.entity.Notice;
import com.lingnan.fruitshop.service.HomeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer/home")
public class HomeController {

    private final HomeService homeService;

    public HomeController(HomeService homeService) {
        this.homeService = homeService;
    }

    @GetMapping("/index")
    public ApiResponse<HomeIndexResponse> index() {
        return ApiResponse.success(homeService.index());
    }

    @GetMapping("/banners")
    public ApiResponse<List<HomeIndexResponse.BannerItem>> banners(@RequestParam(required = false) String position) {
        return ApiResponse.success(homeService.banners(position));
    }

    @GetMapping("/notices")
    public ApiResponse<PageResponse<Notice>> notices(@RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.success(homeService.notices(page, pageSize));
    }
}
