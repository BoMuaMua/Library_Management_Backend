package com.library.borrow.entity.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 借阅记录查询结果VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BorrowRecordVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 借阅记录ID
     */
    private Integer borrowId;

    /**
     * 馆藏位置ID
     */
    private Integer locationId;

    /**
     * 馆藏具体位置
     */
    private String location;

    /**
     * 图书ID
     */
    private Integer bookId;

    /**
     * 图书名称
     */
    private String bookTitle;

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 用户编号（学号/工号）
     */
    private String userCode;

    /**
     * 借阅时间
     */
    private LocalDateTime borrowTime;

    /**
     * 应还时间
     */
    private LocalDateTime dueReturnTime;

    /**
     * 实际归还时间
     */
    private LocalDateTime actualReturnTime;

    /**
     * 逾期罚金金额
     */
    private BigDecimal penalty;

    /**
     * 借阅状态，如 0-借阅中, 1-已还, 2-逾期未还
     */
    private Integer borrowStatus;

    /**
     * 罚金支付状态，如 0-无需支付/已支付, 1-未支付
     */
    private Integer paymentStatus;
}
