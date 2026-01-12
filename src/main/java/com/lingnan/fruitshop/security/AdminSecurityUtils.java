package com.lingnan.fruitshop.security;

import com.lingnan.fruitshop.common.exception.BizException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class AdminSecurityUtils {

    private AdminSecurityUtils() {
    }

    public static long currentAdminId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null || "anonymousUser".equals(authentication.getPrincipal())) {
            throw BizException.unauthorized("未登录/token过期");
        }
        if (authentication.getPrincipal() instanceof AdminPrincipal principal) {
            return principal.getAdminId();
        }
        throw BizException.unauthorized("未登录/token过期");
    }

    public static AdminPrincipal currentAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null || "anonymousUser".equals(authentication.getPrincipal())) {
            throw BizException.unauthorized("未登录/token过期");
        }
        if (authentication.getPrincipal() instanceof AdminPrincipal principal) {
            return principal;
        }
        throw BizException.unauthorized("未登录/token过期");
    }
}
