package com.library.user.service;

import com.library.user.entity.User;
import com.library.user.entity.vo.UserNewVO;
import com.library.user.entity.vo.UserQueryVO;
import gaarason.database.appointment.Paginate;


public interface UserService {
    // 返回框架自带的 Paginate 分页对象


    Paginate<User> getUserList(Integer page, Integer pageSize);

    /**
     * 根据学号获取用户的角色权限代码
     *
     * @param userId 学号
     * @return 角色权限代码 sys_role_code
     */
    String getSysRoleCodeByUserId(String userId);

    Boolean newUser(UserNewVO userNewVO);

    Boolean lostAccount(Integer userId);

    Boolean deleteAccount(Integer userId);

    User getUserInfo(String userId);

}