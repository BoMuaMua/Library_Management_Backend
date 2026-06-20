package com.library.book.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;

/**
 * 图书馆藏位置及状态实体类 (book_location)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookLocation implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 位置ID，主键 (int(11))
     */
    private Integer locationId;

    /**
     * 图书ID，关联book表 (int(11))
     */
    private Integer bookId;

    /**
     * 馆藏具体位置，如"三楼西侧借阅区-A流水架" (varchar(255))
     */
    private String location;

    /**
     * 在馆状态，如 0-在馆、1-借出、2-遗失 (tinyint(4))
     * tinyint(4) 在 Java 中推荐使用 Integer 或 Byte 承接
     */
    private Integer status;
}
