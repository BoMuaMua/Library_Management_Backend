package com.library.borrow.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 借阅策略实体类 (borrow_strategies)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BorrowStrategies implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 策略ID (int(11))
     */
    private Integer id;

    /**
     * 角色代码，如 0-学生, 1-教师 (tinyint(4))
     */
    private Integer roleCode;

    /**
     * 最大可借册数 (tinyint(4))
     */
    private Integer maxBooks;

    /**
     * 最大可借天数 (int(11))
     */
    private Integer maxDays;

    /**
     * 可续借次数 (tinyint(4))
     */
    private Integer renewTimes;

    /**
     * 每日限制册数/单日借阅上限 (tinyint(4))
     */
    private Integer dailyLimit;

    /**
     * 每日逾期罚金 (decimal(10,2))
     */
    private BigDecimal dailyPenalty;

    /**
     * 最高罚金上限 (decimal(10,2))
     */
    private BigDecimal maxPenaltyLimit;
}
