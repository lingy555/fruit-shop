package com.lingnan.fruitshop.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lingnan.fruitshop.common.api.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.PrintWriter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, ObjectMapper objectMapper) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.objectMapper = objectMapper;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/upload").permitAll()
                        .requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()
                        .requestMatchers("/api/customer/auth/**").permitAll()
                        .requestMatchers("/api/customer/home/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/customer/product/categories").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/customer/product/list").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/customer/product/detail/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/customer/product/comments/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/customer/product/hotKeywords").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/customer/shop/myFavorites").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.POST, "/api/customer/shop/favorite").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.DELETE, "/api/customer/shop/favorite/**").hasRole("CUSTOMER")

                        .requestMatchers(HttpMethod.GET, "/api/customer/shop/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/customer/shop/*/products").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/admin/auth/captcha").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/admin/auth/login").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        .requestMatchers("/api/merchant/auth/**").permitAll()
                        .requestMatchers("/api/merchant/**").hasRole("MERCHANT")

                        .requestMatchers("/api/customer/**").hasRole("CUSTOMER")
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(eh -> eh
                        .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.setCharacterEncoding("UTF-8");
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    ApiResponse<Void> body = ApiResponse.fail(401, "未登录/token过期");
                    try (PrintWriter writer = response.getWriter()) {
                        writer.write(objectMapper.writeValueAsString(body));
                        writer.flush();
                    }
                })
                        .accessDeniedHandler(accessDeniedHandler()))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setCharacterEncoding("UTF-8");
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            ApiResponse<Void> body = ApiResponse.fail(403, "无权限访问");
            try (PrintWriter writer = response.getWriter()) {
                writer.write(objectMapper.writeValueAsString(body));
                writer.flush();
            }
        };
    }
}
