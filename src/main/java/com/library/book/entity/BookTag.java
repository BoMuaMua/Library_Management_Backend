package com.library.book.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;

/**
 * 图书标签实体类 (BookTag)
 * 对应数据库表 booktag
 * book_id 作为外键关联 book 表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookTag implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID (int(11))
     */
    private Integer id;

    /**
     * 书籍ID，外键关联book表 (int(11))
     */
    private Integer bookId;

    /**
     * 标签名称 (varchar(100))
     */
    private String tagName;
}
