package com.library.auth.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Objects;

@Component
@Slf4j
public class JwtUtils {

    @Autowired
    public StringRedisTemplate stringRedisTemplate;

    @Value("${jwt.secret}")
    private String SECRET_KEY;


    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }


    /**
     * 生成访问令牌 AccessToken
     *
     * @param studentNum 学号
     * @param role 角色
     * @return 签名的 JWT
     */
    public String createAccessToken(String studentNum, Integer role) {
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        Date shortExp = new Date(nowMillis + getRemainingTime("access") * 1000L);
        Date longExp = new Date(nowMillis + getRemainingTime("refresh") * 1000L);

        return Jwts.builder()
                .setSubject("StudentAuth") // 主题
                .claim("StudentNum", studentNum) // 自定义字段：学号
                .claim("Role", role) // 自定义字段：角色
                .claim("shortExp", shortExp.getTime()/1000) // 短期过期时间（访问令牌）
                .claim("longExp", longExp.getTime()/1000) // 长期过期时间（刷新令牌）
                .setIssuedAt(now) // 签发时间 (iat)
                .setExpiration(longExp) // 使用长期过期时间作为JWT的标准过期时间
                .signWith(key, SignatureAlgorithm.HS256) // 签名算法
                .compact();
    }



    /**
     * 获取当日零点前的剩余时间
     */
    public long getSecondsUntilMidnight() {
        LocalDateTime now = LocalDateTime.now();

        // 获取明天的 00:00:00
        LocalDateTime midnight = LocalDate.now().plusDays(1).atStartOfDay();

        // 计算两者之间的秒数差
        return Duration.between(now, midnight).getSeconds();
    }

    /**
     * 执行Token的“次日零点强制熔断”的核心逻辑
     *
     * @param type access:访问令牌 ; refresh:刷新令牌
     */
    public long getRemainingTime(String type) {
        long TTL = 1;

        // 1. 计算距离今天结束还有多少秒
        long secondsUntilMidnight = getSecondsUntilMidnight();
        // 如果距离午夜不到 5 分钟，索性给它一个最小宽限期，避免刚登录就掉线
        if (secondsUntilMidnight < 300) {
            secondsUntilMidnight = 300;
        }

        if (Objects.equals(type, "access")) {
            // 2. 计算 Access Token 的 TTL (30分钟 = 1800秒)
            TTL = Math.max(1, Math.min(1800, secondsUntilMidnight));
        }
        if (Objects.equals(type, "refresh")) {
            // 3. 计算 Refresh Token 的 TTL (24小时 = 86400秒)
            TTL = Math.max(1, Math.min(86400, secondsUntilMidnight));
        }
        if (TTL > 0) {
            return TTL;
        } else
            return 1;
    }


    /**
     * 解析 Token
     *
     * @param token JWT 字符串
     * @return Claims 对象，包含主题和自定义字段等信息
     */
    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 从 Token 中提取学号（即使 Token 过期也能获取）
     *
     * @param token JWT 令牌
     * @return 学号
     */
    public String getStudentNumFromToken(String token) {
        if (StringUtils.isBlank(token)) {
            log.error("尝试解析空的 JWT Token");
            return null;
        }
        try {
            Claims claims = parseToken(token);
            return claims.get("StudentNum", String.class);
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            // Token 过期了，但仍然可以从异常中获取 Claims
            return e.getClaims().get("StudentNum", String.class);
        }
    }


    /**
     * 检查 Token 是否已过期
     *
     * @param token JWT 令牌
     * @return true 表示已过期，false 表示未过期
     */
    public boolean isShortTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            Long shortExp = claims.get("shortExp", Long.class);

            // 检查短期过期时间是否小于当前时间
            if (shortExp != null) {
                return shortExp < System.currentTimeMillis();
            }

            // 如果没有shortExp字段，则检查标准过期时间
            Date expiration = claims.getExpiration();
            return expiration.before(new Date());

        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            // Token 已过期，直接返回true
            return true;
        } catch (Exception e) {
            log.error("解析Token失败: {}", e.getMessage());
            return true;
        }
    }

    /**
     * 获取当前线程上下文中的 Token 里的学号
     *
     * @return studentNum
     */
    public Object getStudentNumFromCurrentToken() {
       /* Spring Security 在认证成功后，为了安全会默认执行 eraseCredentials()。这会导致 Authentication 对象内部的 credentials 字段被清空。
        在第一张图（日志）中：它显示为 [PROTECTED]。
        在第二张图（代码执行）中：当程序运行到这一行时，由于“钥匙”已经被系统销毁（擦除）了，所以你拿到的就是个空。*/
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // 增加健壮性判断：防止匿名访问时 authentication 为空导致空指针
        if (authentication != null && authentication.isAuthenticated()) {
            if (authentication instanceof AnonymousAuthenticationToken) {
                log.error("当前是匿名用户，请检查 Filter 是否成功 setAuthentication");
            }
            return (String) authentication.getPrincipal();
        }
        return null;
    }

    //判断token里的longExp是否过期
    public boolean isLongTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            // 优先判断自定义的 longExp
            Long longExp = claims.get("longExp", Long.class);
            if (longExp != null) {
                return longExp < (System.currentTimeMillis() / 1000L);
            }

            // 如果没有 longExp，则使用标准过期时间
            Date expiration = claims.getExpiration();
            return expiration != null && expiration.before(new Date());

        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            // 如果标准的 exp 过期了，但你还想检查 longExp，
            // 那么你需要从 e.getClaims() 中获取数据，因为 parseToken 已经失败了
            Claims claims = e.getClaims();
            Long longExp = claims.get("longExp", Long.class);
            if (longExp != null) {
                return longExp < (System.currentTimeMillis() / 1000L);
            }
            return true;
        } catch (Exception e) {
            log.error("解析Token失败: {}", e.getMessage());
            return true;
        }
    }
}

