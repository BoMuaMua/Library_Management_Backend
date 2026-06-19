package com.library.user.model;



import com.library.user.entity.User;
import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.contract.eloquent.Builder;
import gaarason.database.eloquent.Model;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class UserModel extends Model<UserQuery, User, Integer> {

    @Resource
    private GaarasonDataSource gaarasonDataSource;

    /**
     * 快捷查询方法：返回值也完美对齐为 Builder<User, Integer, ?>
     */
    public UserQuery baseQuery() {
        // 这样写，类型安全，where 绝对能认出来，并且在 Service 里点出来的 where、orderBy 都有完整的 User 字段语法提示！
        return this.newQuery().where("is_deleted", 0);
    }

    /**
     * 实现父类的方法：直接注入并返回 Spring 容器中的数据源，不要返回 null
     */
    @Override
    public GaarasonDataSource getGaarasonDataSource() {
        return gaarasonDataSource;
    }

}
