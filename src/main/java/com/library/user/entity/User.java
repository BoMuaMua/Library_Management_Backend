package com.library.user.entity;

import gaarason.database.annotation.Column;
import gaarason.database.annotation.Primary;
import gaarason.database.annotation.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user")
public class User implements Serializable{

    private static final long serialVersionUID = 1L;

    @Primary
    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "phone")
    private String phone;

    @Column(name = "user_code")
    private String userCode;

    @Column(name = "password")
    private String password;

    @Column(name = "role_code")
    private Integer roleCode;

    @Column(name = "sys_role_code")
    private String sysRoleCode;

    @Column(name = "status")
    private Integer status;

    @Column(name = "is_deleted")
    private Integer isDeleted;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    // 快捷生成所有的 Getters / Setters...
}
