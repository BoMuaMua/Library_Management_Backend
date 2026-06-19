package com.library.user.controller;

import com.library.auth.common.Result;
import com.library.user.entity.User;
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
}