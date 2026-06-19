/**
 * JWT 认证过滤器，用于处理基于 JWT 的身份验证流程
 * <p>
 * 该过滤器负责在每次请求时校验 JWT 令牌的有效性，并在必要时刷新令牌。
 * 如果令牌有效，会将用户信息存入 Spring Security 的上下文中，以便后续组件使用。
 * </p>
 */

package com.library.auth.filter;


import com.library.auth.service.AuthService;
import com.library.auth.util.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;


@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {


    private JwtUtils jwtUtils;
    private StringRedisTemplate redisTemplate;

    private SystemUsersMapper systemUsersMapper;
    private AuthService authService;

    private final SecurityContextRepository securityContextRepository = new RequestAttributeSecurityContextRepository();


    /**
     * 构造函数，用于初始化 JWT 工具类、Redis 模板、用户映射器和认证服务
     *
     * @param jwtUtils           JWT 工具类，用于处理 JWT 的生成与解析
     * @param redisTemplate      Redis 模板，用于操作 Redis 数据库
     * @param systemUsersMapper  用户数据访问层，用于数据库操作
     * @param authService        认证服务，用于处理认证相关逻辑
     */
    public JwtAuthenticationFilter(JwtUtils jwtUtils, StringRedisTemplate redisTemplate, SystemUsersMapper systemUsersMapper, AuthService authService) {
        this.jwtUtils = jwtUtils;
        this.redisTemplate = redisTemplate;
        this.systemUsersMapper = systemUsersMapper;
        this.authService = authService;
    }

    // 定义白名单，不需要 Token 的路径放这里
    private static final List<String> WHITE_LIST = List.of("/login");




    /**
     * 内部过滤方法，用于处理请求的 JWT 认证逻辑
     * <p>
     * 1. 检查请求路径是否在白名单中，如果是则直接放行；
     * 2. 处理跨域预检请求（OPTIONS）；
     * 3. 获取并校验请求头中的 JWT 令牌；
     * 4. 检查令牌是否在黑名单中；
     * 5. 解析 JWT 令牌内容，判断是否需要刷新令牌；
     * 6. 如果需要刷新，生成新的短时效令牌，并将旧令牌加入黑名单；
     * 7. 将用户信息存入 SecurityContext，以便后续组件使用；
     * 8. 继续向后传递请求。
     * </p>
     *
     * @param request     HTTP 请求对象，用于获取请求信息
     * @param response    HTTP 响应对象，用于设置响应信息
     * @param filterChain 过滤器链，用于继续处理请求
     * @throws ServletException 如果请求处理过程中发生异常
     * @throws IOException      如果 I/O 操作过程中发生异常
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 获取请求路径和方法
        String path = request.getServletPath();
        String method = request.getMethod();
        log.info(">>> 收到请求: " + method + " " + path);

        // 白名单路径直接放行
        if (WHITE_LIST.contains(path)) {
            log.info(">>> 命中白名单，直接放行!");
            filterChain.doFilter(request, response);
            return;
        }

        // 2. 处理 OPTIONS 请求（跨域预检请求直接放行）
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. 获取 Token
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            handleException(response, "未提供验证令牌 (Token)");
            return;
        }

        // 检查 token 是否在 Redis 黑名单中
        String blackListKey = "auth:blacklist:" + token;
        String blacklistedToken = redisTemplate.opsForValue().get(blackListKey);
        if (blacklistedToken != null && "1".equals(blacklistedToken)) {
            handleException(response, "Token 已被加入黑名单，请重新登录");
            return;
        }

        // 4. 解析 JWT 基础信息
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            handleException(response, "请求未携带合法的 Bearer Token");
            return;
        }
        token = header.substring(7).trim(); // 必须从第7位开始截取
        Claims claims;

        //判断shortExp是否过期
        if (jwtUtils.isShortTokenExpired(token)) {
            // 检查 longExp 是否过期
            if (!jwtUtils.isLongTokenExpired(token)) {
                try {
                    // longExp 未过期，生成新的短时效 token
                    String studentNumToRefresh = jwtUtils.getStudentNumFromToken(token);
                    Object role = jwtUtils.parseToken(token).get("Role");
                    String newAccessToken = jwtUtils.createAccessToken(studentNumToRefresh, (Integer) role);

                    // 将旧的 token 加入黑名单
                    blackListKey = "auth:blacklist:" + token;
                    long remainingTime = jwtUtils.getRemainingTime("refresh"); // 获取 long token 的剩余过期时间
                    if (remainingTime > 0) {
                        redisTemplate.opsForValue().set(blackListKey, "1", remainingTime, TimeUnit.SECONDS);
                    }




                    // 将新 token 放入响应头，告知客户端更新
                    response.setHeader("Authorization", "Bearer " + newAccessToken);

                    // 使用新 token 继续后续流程
                    token = newAccessToken;
                    claims = jwtUtils.parseToken(token); // 重新解析新 token

                } catch (Exception refreshEx) {
                    handleException(response, "Token 刷新失败：" + refreshEx.getMessage());
                    return;
                }
            } else {
                // longExp 也已过期
                handleException(response, "Token 已过期，请重新登录");
                return;
            }
        } else {
            // 如果令牌没有过期，则正常解析
            claims = jwtUtils.parseToken(token);
        }
        // 获取角色字段
        Integer role = jwtUtils.parseToken(token).get("Role", Integer.class);
        String studentNum = jwtUtils.parseToken(token).get("StudentNum", String.class);

        if (studentNum != null && role != null) {
            // 注意：Spring Security 角色通常需要 "ROLE_" 前缀，或者在匹配时指定
            List<SimpleGrantedAuthority> authorities;
            authorities =
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + getRoleName(role)));

            // 5. 将带有 "Bearer " 前缀的 token 设置回请求头，以便后续组件使用
            String tokenWithBearer = "Bearer " + token;
            request.setAttribute("Authorization", tokenWithBearer);
            // 6. 校验通过，存入 Request 属性供后续 Controller 使用
            request.setAttribute("currentStudentNum", studentNum);
            request.setAttribute("currentToken", tokenWithBearer); // 将当前使用的 token 存入请求属性



            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(studentNum, tokenWithBearer, authorities);

            //设置到当前线程的 Context 中
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);

            log.info("Filter 结束前上下文: " + SecurityContextHolder.getContext().getAuthentication());

            // --- 新增下面这一行，这是 Spring Boot 3 的核心变动 ---
            //这就是导致已经设置的请求头变化为匿名用户的核心问题，需要注意
            securityContextRepository.saveContext(context, request, response);
        }
        // 继续向后传递
        filterChain.doFilter(request, response);


    }

    /**
     * 构建统一的错误返回 JSON
     * <p>
     * 当 Token 验证失败时，返回标准的 401 错误响应，包含错误码、错误消息和数据字段。
     * 响应格式为 JSON，字符集为 UTF-8。
     *
     * @param response HTTP 响应对象，用于设置响应状态和写入 JSON 数据
     * @param msg      错误消息，将作为响应体的 msg 字段返回
     * @throws IOException IO 异常，可能在写入响应流时发生
     */
    private void handleException(HttpServletResponse response, String msg) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401

        // 构建简单的 JSON 字符串
        String json = String.format("{\"code\": 401, \"msg\": \"%s\", \"data\": null}", msg);
        response.getWriter().write(json);
    }

//TODO 这里需要修改
}
