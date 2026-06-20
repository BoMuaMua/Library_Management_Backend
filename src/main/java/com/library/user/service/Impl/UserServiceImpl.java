package com.library.user.service.Impl;

import com.library.user.entity.User;
import com.library.user.entity.vo.UserNewVO;
import com.library.user.entity.vo.UserQueryVO;
import com.library.user.model.UserModel;
import com.library.user.service.UserService;

import gaarason.database.appointment.OrderBy;
import gaarason.database.appointment.Paginate;
import gaarason.database.contract.eloquent.Builder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserModel userModel;


    @Override
    public Paginate<User> getUserList(Integer page , Integer pageSize) {
        // 1. 拿到未删除的基本查询构造器
        // 2. 按照创建时间倒序，如果时间相同则按用户ID正序（保证分页稳定）
        // 3. 执行 Gaarason 的分页查询
        return userModel.baseQuery()
                .orderBy("status",OrderBy.ASC)
                .orderBy("create_time", OrderBy.DESC)
                .orderBy("user_id", OrderBy.ASC)
                .paginate(page, pageSize);
    }

    @Override
    public String getSysRoleCodeByStudentNum(String studentNum) {
        // 1. 根据学号查询用户信息，获取 Record 对象
        var record = userModel.baseQuery()
                .where("sys_role_code", studentNum)
                .limit(1)
                .first();

        // 2. 如果记录不存在，返回 null
        if (record == null) {
            return null;
        }

        // 3. 从 Record 中获取 User 实体
        User user = record.getEntity();

        // 4. 返回用户的角色权限代码
        return user.getSysRoleCode();
    }

    @Override
    public Boolean newUser(UserNewVO userNewVO) {
        LocalDateTime now = LocalDateTime.now();
        User user = new User();
        user.setNickname(userNewVO.getNickname());
        if(userNewVO.getPhone() != null && !userNewVO.getPhone().isEmpty()){
            user.setPhone(userNewVO.getPhone());
        }
        if (userNewVO.getUserCode() != null && !userNewVO.getUserCode().isEmpty()){
            user.setUserCode(userNewVO.getUserCode());
        }
        user.setPassword(userNewVO.getPassword());
        user.setRoleCode(1);
        user.setSysRoleCode("ROLE_USER");
        user.setStatus(1);
        user.setIsDeleted(0);
        user.setCreateTime(now);

        int rows = userModel.newQuery().data(user).insert();


        return rows > 0;
    }
}
