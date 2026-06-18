/**
 * AuthServiceImpl 提供了与用户身份验证相关的业务逻辑实现。
 * 包括登录、登出功能，并支持基于时间的登录限制。
 */

package com.library.auth.service.impl;

import com.library.auth.service.AuthService;
import com.library.auth.util.JwtUtils;
import com.library.util.RedisUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Service
public class AuthServiceImpl implements AuthService {
    @Autowired
    private SystemUsersService systemUsersService;
    @Autowired
    private SystemUsersMapper  systemUsersMapper;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RedisUtils redisUtils;





    /**
     * 用户登录接口。
     * 根据配置决定是否启用登录时间限制，若启用则验证当前时间是否在允许登录时间段内。
     *
     * @param studentNum 学号，用于用户身份识别
     * @param password   登录密码，用于身份验证
     * @return 登录成功后生成的 JWT 访问令牌
     */
    @Override
    public String login(Long studentNum, String password) {
        return executeLogin(studentNum, password);
    }


    /**
     * 执行实际的登录验证逻辑。
     * 包括学号格式校验、密码格式校验、用户状态校验等。
     *
     * @param studentNum 学号，用于用户身份识别
     * @param password   登录密码，用于身份验证
     * @return 登录成功后生成的 JWT 访问令牌
     */
    public String executeLogin(Long studentNum, String password) {

        // 验证 StudentNum 有 12 位
        if (studentNum == null || String.valueOf(studentNum).length() != 12) {
            throw new IllegalArgumentException("学号必须为 12 位数字");
        }
        // 验证 password 满足 gcu@123 后加两个字母以上的格式
        if (password == null || !password.matches("^gcu@123[a-zA-Z]{2,}$")) {
            throw new IllegalArgumentException("密码格式错误，应为 gcu@123 后跟两个或以上字母");
        }
        SystemUsers users = systemUsersService.getUserByLogin(studentNum, password);
        if (users.getUsername() == null) {
            throw new RuntimeException("用户不存在或密码错误");
        }
        if (users.getStatus()==0){
            throw new RuntimeException("用户未启用");
        }

        //获取Token
        String accessToken = jwtUtils.createAccessToken(studentNum.toString(),users.getRole());

        //将当前登录时间当做最后登录时间
        systemUsersMapper.setLastLoginTime(setLastLoginTime(), studentNum);
        return accessToken;
    }


    /**
     * 用户登出接口。
     * 从请求头中提取 Token，将其加入黑名单，并清除安全上下文中的认证信息。
     *
     * @param request HTTP 请求对象，用于提取 Token
     * @return 登出结果，固定返回 "退出成功"
     */
    @Override
    public String logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            // 打印接收到的 token
            System.out.println("接收到的 Token: " + token);

            // 将token加入Redis中的黑名单
            redisUtils.deleteToken(token);
            //删除上下文的token
            SecurityContextHolder.clearContext();

        }
        return "退出成功";
    }

    /**
     * 设置用户最后登录时间。
     * 返回当前时间作为最后登录时间。
     *
     * @return 表示当前时间的 LocalDateTime 对象
     */
    public LocalDateTime setLastLoginTime() {
        return LocalDateTime.now();
    }
}

//TODO 这里需要修改




