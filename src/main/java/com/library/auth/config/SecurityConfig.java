/**
 * Spring Security 配置类
 *
 * 负责配置应用的安全策略，包括：
 * 1. 禁用 CSRF 保护（JWT 存储在 Header 中，天然免疫 CSRF）
 * 2. 禁用表单登录和 HTTP Basic 认证（使用 JWT 认证）
 * 3. 禁用 Session（无状态认证，交给 JWT 管理）
 * 4. 配置自定义异常处理（返回 JSON 格式错误）
 * 5. 注册 JWT 认证过滤器
 *
 * @author BoMuaMua
 * @version 1.0
 * @date 2026-03-28
 */
package com.library.auth.config;

import com.library.auth.filter.JwtAuthenticationFilter;
import com.library.auth.service.AuthService;
import com.library.auth.util.JwtUtils;
import com.library.user.model.UserModel;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // 开启注解控制权限，如 @PreAuthorize
public class SecurityConfig {

    /**
     * 配置安全过滤器链
     *
     * 定义 Spring Security 的核心配置，包括：
     * 1. 禁用 CSRF 保护（JWT 存储在 Header 中，天然免疫 CSRF）
     * 2. 禁用默认的表单登录（防止 302 重定向到 /login）
     * 3. 禁用 HTTP Basic 认证（防止弹窗登录）
     * 4. 禁用注销功能（JWT 不依赖 Session）
     * 5. 禁用 Session（设置为无状态，交给 JWT 管理）
     * 6. 配置路径拦截规则（所有请求都放行，交给 JWT 过滤器处理）
     * 7. 配置异常处理：
     *    - authenticationEntryPoint: 未登录或 Token 失效时返回 401
     *    - accessDeniedHandler: 权限不足时返回 403
     * 8. 注册 JWT 认证过滤器（在 UsernamePasswordAuthenticationFilter 之前执行）
     *
     * @param http HttpSecurity 对象，用于配置 HTTP 安全策略
     * @return SecurityFilterChain 安全过滤器链，包含所有配置的安全过滤器
     * @throws Exception 配置过程中可能抛出的异常
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
                // 1. 禁用 CSRF（因为 JWT 存储在 Header 中，天然免疫 CSRF）
                .csrf(AbstractHttpConfigurer::disable)
                // 2. 禁用默认的表单登录（防止 302 重定向到 /login）
                .formLogin(AbstractHttpConfigurer::disable)
                // 3. 禁用 HTTP Basic 认证（弹窗登录）
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .securityContext(context -> context.requireExplicitSave(false))
                // 4. 禁用 Session（设置为无状态，交给 JWT 管理）
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()  // ✅ 所有请求都放行，交给 JWT 过滤器处理
                )
                // 5. 核心：配置异常处理
                .exceptionHandling(exceptions -> exceptions
                        // 当未登录或 Token 失效时，不重定向，而是执行自定义逻辑
                        .authenticationEntryPoint((request, response, authException) -> {
                            // 自定义未登录处理：返回 401 错误码和 JSON
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setCharacterEncoding("UTF-8");
                            response.setContentType("application/json");
                            response.getWriter().write("{\"code\": 401, \"msg\": \"请先登录\"}");
                        })
                        // 当权限不足时（已登录但没权限）
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.getWriter().write("{\"code\": 403, \"msg\": \"权限不足\", \"data\": null}");
                        })

                )
                // TODO: 临时关闭权限验证 - 注释掉 JWT 过滤器注册
                // .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        ;

        return http.build();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtUtils jwtUtils, StringRedisTemplate redisTemplate, UserModel userModel, AuthService authService) {
        // 这里手动传入依赖，保证 Filter 启动时依赖已经准备就绪
        return new JwtAuthenticationFilter(jwtUtils, redisTemplate, userModel, authService);
    }
//TODO 这里需要修改
}
