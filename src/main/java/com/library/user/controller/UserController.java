package com.library.user.controller;

import com.library.auth.common.Result;
import com.library.user.entity.User;
import com.library.user.entity.vo.UserNewVO;
import com.library.user.entity.vo.UserQueryVO;
import com.library.user.service.UserService;
import gaarason.database.appointment.Paginate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 获取用户分页列表
     * 支持根据昵称、手机号、状态进行筛选
     */
    @GetMapping("/list")
    public Result list(@RequestParam Integer page, @RequestParam Integer pageSize) {
        // 调用业务层
        Paginate<User> userPaginate = userService.getUserList(page, pageSize);

        // 返回统一的响应格式（这里的 Result 是你项目里自定义的统一返回包装类）
        return Result.success(userPaginate);
    }

    @PostMapping("/new")
    public Result newUser(@RequestBody UserNewVO userNewVO) {
        return userService.newUser(userNewVO) ? Result.success("注册成功") : Result.error(500, "注册失败，该用户已存在");
    }

    /**
     * 获取当前用户的角色权限代码
     *
     * @param userId 学号（从 JWT Token 中获取）
     * @return 角色权限代码 sys_role_code
     */
    @GetMapping("/role")
    public Result getSysRoleCode(@RequestParam String userId) {
        String sysRoleCode = userService.getSysRoleCodeByUserId(userId);
        
        if (sysRoleCode == null) {
            return Result.error(404, "用户不存在或未分配角色权限");
        }
        
        return Result.success(sysRoleCode);
    }
    
    @GetMapping("/info")
    public Result getUserInfo(@RequestParam String userId) {
        User user = userService.getUserInfo(userId);
        
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        
        return Result.success(user);
    }

    /**
     * 挂失读者
     * @param userId 用户ID
     *
     * */
    @PutMapping("/lost")
    public Result lostAccount(@RequestParam Integer userId){
        return userService.lostAccount(userId) ? Result.success("成功") : Result.error(500, "失败");
    }

    /**
     * 注销用户
     * @param userId 用户ID
     *
     * */
    @PostMapping("/delete")
    public Result delete(@RequestBody Integer userId){
        return userService.deleteAccount(userId) ? Result.success("成功") : Result.error(500, "失败");
    }
}