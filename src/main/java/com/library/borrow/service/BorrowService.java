package com.library.borrow.service;

import com.library.borrow.entity.vo.BorrowRecordVO;
import gaarason.database.appointment.Paginate;

import java.math.BigDecimal;

/**
 * 借阅服务接口
 */
public interface BorrowService {

    /**
     * 查询借阅记录列表（分页）
     * 支持通过书籍名称、用户昵称、学工号、电话号码、借阅时间、罚款状态查询
     * 
     * @param page 页码
     * @param limit 每页数量
     * @param bookName 书籍名称（可选，模糊查询）
     * @param userName 用户昵称（可选，模糊查询）
     * @param userCode 学工号（可选，模糊查询）
     * @param phone 电话号码（可选，模糊查询）
     * @param borrowTimeStart 借阅时间起始（可选）
     * @param borrowTimeEnd 借阅时间结束（可选）
     * @param paymentStatus 罚金支付状态（可选）0-无需支付/已支付, 1-未支付
     * @return 分页结果
     */
    Paginate<BorrowRecordVO> getBorrowRecords(Integer page, Integer limit, 
                                               String bookName, String userName,
                                               String userCode, String phone,
                                               String borrowTimeStart, String borrowTimeEnd,
                                               Integer paymentStatus);

    /**
     * 缴费登记
     * 修改借阅记录的罚金支付状态为已支付，并更新罚金金额
     * 
     * @param borrowId 借阅记录ID
     * @param penaltyAmount 缴纳的罚金金额
     * @return 是否成功
     */
    Boolean payPenalty(Integer borrowId, BigDecimal penaltyAmount);

    /**
     * 查询罚款记录列表（分页）
     * 仅查看借阅记录表中逾期记录，排序：未缴罚款排在已缴前面，二级排序按逾期时间先后
     * 
     * @param page 页码
     * @param limit 每页数量
     * @param bookName 书籍名称（可选，模糊查询）
     * @param userName 用户昵称（可选，模糊查询）
     * @param userCode 学工号（可选，模糊查询）
     * @param phone 电话号码（可选，模糊查询）
     * @param paymentStatus 罚金支付状态（可选）0-无需支付/已支付, 1-未支付
     * @return 分页结果
     */
    Paginate<BorrowRecordVO> getPenaltyRecords(Integer page, Integer limit,
                                               String bookName, String userName,
                                               String userCode, String phone,
                                               Integer paymentStatus);
}
