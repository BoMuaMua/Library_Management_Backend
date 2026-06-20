package com.library.borrow.model;

import com.library.borrow.entity.BorrowingRecord;
import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.contract.eloquent.Builder;
import gaarason.database.eloquent.Model;
import gaarason.database.query.QueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class BorrowingRecordModel extends Model<QueryBuilder<BorrowingRecord, Integer>, BorrowingRecord, Integer> {

    private static ApplicationContext applicationContext;
    private GaarasonDataSource gaarasonDataSource;

    public BorrowingRecordModel() {
    }

    @Autowired
    public void setApplicationContext(ApplicationContext context) {
        BorrowingRecordModel.applicationContext = context;
    }

    @Autowired(required = false)
    public void setGaarasonDataSource(GaarasonDataSource gaarasonDataSource) {
        this.gaarasonDataSource = gaarasonDataSource;
    }

    /**
     * 基础查询构造器
     */
    public Builder<?, BorrowingRecord, Integer> baseQuery() {
        return this.newQuery();
    }

    @Override
    public GaarasonDataSource getGaarasonDataSource() {
        if (gaarasonDataSource != null) {
            return gaarasonDataSource;
        }

        if (applicationContext != null) {
            try {
                gaarasonDataSource = applicationContext.getBean(GaarasonDataSource.class);
                return gaarasonDataSource;
            } catch (Exception e) {
                System.err.println("警告: 无法找到 GaarasonDataSource Bean");
            }
        }

        return null;
    }
}
