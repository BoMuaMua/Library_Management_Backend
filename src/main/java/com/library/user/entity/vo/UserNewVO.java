package com.library.user.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserNewVO {
    private String nickname;
    private String phone;
    // 学工号
    private String userCode;
    private String password;
}
