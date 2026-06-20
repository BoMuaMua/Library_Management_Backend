package com.library.borrow.controller;

import com.library.auth.common.Result;
import com.library.borrow.entity.vo.BorrowRecordVO;
import com.library.borrow.service.BorrowService;
import gaarason.database.appointment.Paginate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * 借阅管理控制器
 */
@RestController
@RequestMapping("/api/users")
public class BorrowController {

    @Autowired
    private BorrowService borrowService;

    /**
     * 查询借阅记录列表（分页）
     * GET /api/users/borrow-records
     * 
     * @param page 页码（默认1）
     * @param limit 每页数量（默认10）
     * @param bookName 书籍名称（可选，模糊查询）
     * @param userName 用户昵称（可选，模糊查询）
     * @param userCode 学工号（可选，模糊查询）
     * @param phone 电话号码（可选，模糊查询）
     * @param borrowTimeStart 借阅时间起始（可选），格式：yyyy-MM-dd HH:mm:ss
     * @param borrowTimeEnd 借阅时间结束（可选），格式：yyyy-MM-dd HH:mm:ss
     * @param paymentStatus 罚金支付状态（可选）0-无需支付/已支付, 1-未支付
     * @return 分页结果
     */
    @GetMapping("/borrow-records")
    public Result getBorrowRecords(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false) String bookName,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String userCode,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String borrowTimeStart,
            @RequestParam(required = false) String borrowTimeEnd,
            @RequestParam(required = false) Integer paymentStatus) {
        
        Paginate<BorrowRecordVO> result = borrowService.getBorrowRecords(
                page, limit, bookName, userName, userCode, phone,
                borrowTimeStart, borrowTimeEnd, paymentStatus);
        
        return Result.success(result);
    }

    /**
     * 查看罚款记录
     * GET /api/users/all/asset-penalties
     * 
     * @param page 页码（默认1）
     * @param limit 每页数量（默认10）
     * @param bookName 书籍名称（可选，模糊查询）
     * @param userName 用户昵称（可选，模糊查询）
     * @param userCode 学工号（可选，模糊查询）
     * @param phone 电话号码（可选，模糊查询）
     * @param paymentStatus 罚金支付状态（可选）0-已支付, 1-未支付
     * @return 分页结果
     */
    @GetMapping("/all/asset-penalties")
    public Result getPenaltyRecords(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false) String bookName,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String userCode,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) Integer paymentStatus) {
        
        Paginate<BorrowRecordVO> result = borrowService.getPenaltyRecords(
                page, limit, bookName, userName, userCode, phone, paymentStatus);
        
        return Result.success(result);
    }

    /**
     * 缴费登记
     * POST /api/asset-penalties/{id}/pay
     * 
     * @param id 借阅记录ID
     * @param penaltyAmount 缴纳的罚金金额
     * @return 操作结果
     */
    @PostMapping("/asset-penalties/{id}/pay")
    public Result payPenalty(
            @PathVariable("id") Integer id,
            @RequestParam BigDecimal penaltyAmount) {
        
        Boolean success = borrowService.payPenalty(id, penaltyAmount);
        if (success) {
            return Result.success("缴费成功");
        } else {
            return Result.error(500, "缴费失败，请检查记录状态或参数");
        }
    }
}
