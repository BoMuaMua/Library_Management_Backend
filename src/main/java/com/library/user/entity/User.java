package com.library.user.entity;

import gaarason.database.annotation.Column;
import gaarason.database.annotation.Primary;
import gaarason.database.annotation.Table;
import gaarason.database.contract.eloquent.Builder;

import java.io.Serializable;
import java.util.Date;

@Table(name = "user")
public class User implements Serializable{

    @Primary
    @Column(name = "user_id", unsigned = true)
    private Integer userId;

    private String nickname;
    private String phone;

    @Column(name = "user_code")
    private String userCode;

    private String password;

    @Column(name = "role_code")
    private Integer roleCode;

    @Column(name = "sys_role_code")
    private String sysRoleCode;

    private Integer status;

    @Column(name = "is_deleted")
    private Integer isDeleted;

    @Column(name = "create_time")
    private Date createTime;

    // 快捷生成所有的 Getters / Setters...
}
