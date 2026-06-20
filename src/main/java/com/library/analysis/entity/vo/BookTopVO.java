package com.library.analysis.entity.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;

/**
 * 图书借阅排行榜VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookTopVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 图书ID
     */
    private Integer bookId;

    /**
     * 图书名称
     */
    private String title;

    /**
     * 作者
     */
    private String author;

    /**
     * 分类
     */
    private String classification;

    /**
     * 借阅次数
     */
    private Long borrowCount;
}
