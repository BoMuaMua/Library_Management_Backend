package com.library.analysis.entity.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 库存分析VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryAnalysisVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 图书总种类数
     */
    private Long totalBookTypes;

    /**
     * 图书总册数
     */
    private Long totalBooks;

    /**
     * 已借出册数
     */
    private Long borrowedBooks;

    /**
     * 在库册数
     */
    private Long availableBooks;

    /**
     * 库存利用率（百分比）
     */
    private BigDecimal utilizationRate;

    /**
     * 低库存图书数量（库存<=5）
     */
    private Long lowStockCount;

    /**
     * 零库存图书数量
     */
    private Long zeroStockCount;
}
