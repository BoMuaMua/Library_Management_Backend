package com.library.policies.entity.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 逾期罚金标准VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OverdueFineVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 策略ID
     */
    private Integer id;

    /**
     * 角色代码，如 0-学生, 1-教师, 2-访客
     */
    private Integer roleCode;

    /**
     * 每日逾期罚金
     */
    private BigDecimal dailyPenalty;

    /**
     * 最高罚金上限
     */
    private BigDecimal maxPenaltyLimit;
}
