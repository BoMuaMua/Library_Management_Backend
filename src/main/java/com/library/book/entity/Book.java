package com.library.book.entity;

import gaarason.database.annotation.Column;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;

/**
 * 图书实体类 (Book)
 * 对应数据库表结构
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Book implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 书籍ID (int(11))
     */
    @Column(name = "book_id")
    private Integer bookId;

    /**
     * 书名 (varchar(255))
     */
    private String title;

    /**
     * 作者 (varchar(255))
     */
    private String author;

    /**
     * ISBN号 (varchar(17))
     */
    @Column(name = "ISBN")
    private String isbn;

    /**
     * 分类 (varchar(100))
     */
    private String classification;

    /**
     * 库存数量 (int(11))
     */
    private Integer inventory;

    /**
     * 总借阅次数/时间 (bigint(20))
     */
    @Column(name = "total_borrowing_time")
    private Long totalBorrowingTime;
}
