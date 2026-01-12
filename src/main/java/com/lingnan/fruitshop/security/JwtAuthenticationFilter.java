package com.lingnan.fruitshop.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenUtil jwtTokenUtil;
    private final CustomerUserDetailsService userDetailsService;
    private final MerchantUserDetailsService merchantUserDetailsService;
    private final AdminUserDetailsService adminUserDetailsService;

    public JwtAuthenticationFilter(JwtTokenUtil jwtTokenUtil,
                                    CustomerUserDetailsService userDetailsService,
                                    MerchantUserDetailsService merchantUserDetailsService,
                                    AdminUserDetailsService adminUserDetailsService) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.userDetailsService = userDetailsService;
        this.merchantUserDetailsService = merchantUserDetailsService;
        this.adminUserDetailsService = adminUserDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Jws<Claims> claimsJws = jwtTokenUtil.parseToken(token);
                String subject = claimsJws.getBody().getSubject();
                long id = Long.parseLong(subject);
                String type = claimsJws.getBody().get("type", String.class);
                if (type == null || type.isBlank()) {
                    type = "customer";
                }

                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails;
                    if ("merchant".equalsIgnoreCase(type)) {
                        userDetails = merchantUserDetailsService.loadMerchantById(id);
                    } else if ("admin".equalsIgnoreCase(type)) {
                        userDetails = adminUserDetailsService.loadAdminById(id);
                    } else {
                        userDetails = userDetailsService.loadUserById(id);
                    }
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception ignored) {
            }
        }

        filterChain.doFilter(request, response);
    }
}
