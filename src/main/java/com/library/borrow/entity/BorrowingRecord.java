package com.library.borrow.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 借阅记录实体类 (borrowing_record)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BorrowingRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 借阅记录ID (int(11))
     */
    private Integer borrowId;

    /**
     * 馆藏位置ID，关联 book_location 表 (int(11))
     */
    private Integer locationId;

    /**
     * 用户ID (int(11))
     */
    private Integer userId;

    /**
     * 借阅时间 (datetime)
     */
    private LocalDateTime borrowTime;

    /**
     * 应还时间 (datetime)
     */
    private LocalDateTime dueReturnTime;

    /**
     * 实际归还时间 (datetime)
     */
    private LocalDateTime actualReturnTime;

    /**
     * 逾期罚金金额 (decimal)
     */
    private BigDecimal penalty;

    /**
     * 借阅状态，如 0-借阅中, 1-已还, 2-逾期未还 (tinyint(4))
     */
    private Integer borrowStatus;

    /**
     * 罚金支付状态，如 0-无需支付/已支付, 1-未支付 (tinyint(4))
     */
    private Integer paymentStatus;
}