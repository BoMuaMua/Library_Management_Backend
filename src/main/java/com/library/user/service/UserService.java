package com.library.user.service;

import com.library.user.entity.User;
import com.library.user.entity.vo.UserQueryVO;
import gaarason.database.appointment.Paginate;


public interface UserService {
    // 返回框架自带的 Paginate 分页对象


    Paginate<User> getUserList(Integer page, Integer pageSize);
}