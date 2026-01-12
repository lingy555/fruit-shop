package com.lingnan.fruitshop.security;

import com.lingnan.fruitshop.common.exception.BizException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class SecurityUtils {

    private SecurityUtils() {
    }

    public static long currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null || "anonymousUser".equals(authentication.getPrincipal())) {
            throw BizException.unauthorized("未登录/token过期");
        }
        if (authentication.getPrincipal() instanceof CustomerPrincipal principal) {
            return principal.getUserId();
        }
        throw BizException.unauthorized("未登录/token过期");
    }

    public static Optional<Long> optionalCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null || "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.empty();
        }
        if (authentication.getPrincipal() instanceof CustomerPrincipal principal) {
            return Optional.ofNullable(principal.getUserId());
        }
        return Optional.empty();
    }
}
