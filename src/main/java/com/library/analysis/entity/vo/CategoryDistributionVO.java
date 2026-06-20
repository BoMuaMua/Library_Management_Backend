package com.library.analysis.entity.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;

/**
 * 图书类型分布VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDistributionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 分类名称
     */
    private String category;

    /**
     * 该分类的图书数量
     */
    private Long count;
}
