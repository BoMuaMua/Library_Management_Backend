package com.library.user.model;

import com.library.user.entity.User;
import gaarason.database.contract.eloquent.Builder;

/**
 * 借用 Java 接口继承，直接锁死泛型，终结套娃死循环！
 */
public interface UserQuery extends Builder<UserQuery, User, Integer> {
}
