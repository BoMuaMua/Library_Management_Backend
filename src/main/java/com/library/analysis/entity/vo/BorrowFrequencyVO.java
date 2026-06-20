package com.library.analysis.entity.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;

/**
 * 借阅频次分析VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BorrowFrequencyVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 时间段（如：2026-06-21, 2026-W25周, 2026-06）
     */
    private String timePeriod;

    /**
     * 该时间段的借阅次数
     */
    private Long count;
}
