package com.lingnan.fruitshop.security;

import com.lingnan.fruitshop.common.exception.BizException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class MerchantSecurityUtils {

    private MerchantSecurityUtils() {
    }

    public static long currentMerchantId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null || "anonymousUser".equals(authentication.getPrincipal())) {
            throw BizException.unauthorized("未登录/token过期");
        }
        if (authentication.getPrincipal() instanceof MerchantPrincipal principal) {
            return principal.getMerchantId();
        }
        throw BizException.unauthorized("未登录/token过期");
    }

    public static long currentShopId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null || "anonymousUser".equals(authentication.getPrincipal())) {
            throw BizException.unauthorized("未登录/token过期");
        }
        if (authentication.getPrincipal() instanceof MerchantPrincipal principal) {
            if (principal.getShopId() == null) {
                throw BizException.forbidden("店铺未开通");
            }
            return principal.getShopId();
        }
        throw BizException.unauthorized("未登录/token过期");
    }
}
