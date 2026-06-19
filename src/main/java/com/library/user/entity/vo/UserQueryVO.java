package com.library.user.entity.vo;

import lombok.Data;

@Data
public class UserQueryVO {
    private Integer page = 1;      // 当前页，默认1
    private Integer pageSize = 10; // 每页条数，默认10
    private String nickname;       // 筛选条件：昵称
    private String phone;          // 筛选条件：手机号
    private Integer status;        // 筛选条件：状态
}
