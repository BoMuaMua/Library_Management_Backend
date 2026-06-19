package com.library.user.service.Impl;

import com.library.user.entity.User;
import com.library.user.entity.vo.UserQueryVO;
import com.library.user.model.UserModel;
import com.library.user.service.UserService;

import gaarason.database.appointment.OrderBy;
import gaarason.database.appointment.Paginate;
import gaarason.database.contract.eloquent.Builder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserModel userModel;


    @Override
    public Paginate<User> getUserList(Integer page , Integer pageSize) {
        // 1. 拿到未删除的基本查询构造器
        Builder<?,User,Integer> query = userModel.baseQuery();



        // 3. 按照创建时间倒序
        query.orderBy("create_time", OrderBy.DESC);

        // 4. 执行 Gaarason 超爽的分页查询（传入当前页和页大小）
        // 它内部会自动帮你执行两条 SQL：一条 count 查总数，一条 limit 查分页列表
        return query.paginate(page, pageSize);
    }
}