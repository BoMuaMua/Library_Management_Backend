package com.library.user.service.Impl;

import com.library.user.entity.User;
import com.library.user.entity.vo.UserNewVO;
import com.library.user.entity.vo.UserQueryVO;
import com.library.user.model.UserModel;
import com.library.user.service.UserService;

import gaarason.database.appointment.OrderBy;
import gaarason.database.appointment.Paginate;
import gaarason.database.contract.eloquent.Builder;
import gaarason.database.eloquent.GeneralModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private User.UserModel userModel;

    @Autowired
    private GeneralModel generalModel;


    @Override
    public Paginate<User> getUserList(Integer page , Integer pageSize) {
        // 1. 拿到未删除的基本查询构造器
        // 2. 按照创建时间倒序，如果时间相同则按用户ID正序（保证分页稳定）
        // 3. 执行 Gaarason 的分页查询
        generalModel.newQuery().from("user").get().toMapList();
        return userModel.baseQuery()
                .orderBy("status",OrderBy.ASC)
                .orderBy("create_time", OrderBy.DESC)
                .orderBy("user_id", OrderBy.ASC)
                .paginate(page, pageSize);
    }

    @Override
    public String getSysRoleCodeByUserId(String userId) {
        // 1. 根据学号查询用户信息，获取 Record 对象
        var record = userModel.baseQuery()
                .where("sys_role_code", userId)
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
//TODO 注册用户接口仍旧存在问题，无法修复
    @Override
    public Boolean newUser(UserNewVO userNewVO) {
        LocalDateTime now = LocalDateTime.now();

        System.out.println(userNewVO.getUserCode());
        System.out.println(userNewVO);
        User user = new User();
        // 确保 nickname 不为 null，如果为空则使用默认值
        String nickname = userNewVO.getNickname();
        if (nickname == null || nickname.trim().isEmpty()) {
            nickname = "用户" + System.currentTimeMillis();
        }
        user.setNickname(nickname);
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

        return userModel.newRecord().fillEntity(user).save();
    }

    @Override
    public Boolean lostAccount(Integer userId) {
        int rows = userModel.newQuery()
                .where("user_id", userId)
                .data("status", 3)
                .update();
        return rows > 0;
    }

    @Override
    public Boolean deleteAccount(Integer userId) {
        int rows = userModel.newQuery()
                .where("user_id", userId)
                .data("is_deleted", 1)
                .update();
        return rows > 0;
    }

    @Override
    public User getUserInfo(String userId) {
        return userModel.newQuery()
                .where("user_id", userId)
                .limit(1)
                .firstOrFail()
                .getEntity();
    }
}