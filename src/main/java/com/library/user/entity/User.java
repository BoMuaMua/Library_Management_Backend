package com.library.user.entity;

import com.library.user.model.UserModel;
import gaarason.database.annotation.Column;
import gaarason.database.annotation.Primary;
import gaarason.database.annotation.Table;
import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.contract.eloquent.Builder;
import gaarason.database.eloquent.Model;
import gaarason.database.query.QueryBuilder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

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


}
