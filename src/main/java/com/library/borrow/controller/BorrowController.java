package com.library.borrow.controller;

import com.library.auth.common.Result;
import com.library.borrow.entity.vo.BorrowRecordVO;
import com.library.borrow.service.BorrowService;
import gaarason.database.appointment.Paginate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 借阅管理控制器
 */
@RestController
@RequestMapping("/api/borrow")
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
     * POST /api/borrow/asset-penalties/{id}/pay
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

    /**
     * 借阅图书
     * POST /api/borrow/borrow
     * 
     * @param userId 用户ID
     * @param locationId 馆藏位置ID
     * @return 操作结果
     */
    @PostMapping("/borrow")
    public Result borrowBook(
            @RequestParam Integer userId,
            @RequestParam Integer locationId) {
        
        Boolean success = borrowService.borrowBook(userId, locationId);
        if (success) {
            return Result.success("借阅成功");
        } else {
            return Result.error(500, "借阅失败，请检查参数或库存状态");
        }
    }

    /**
     * 查看自己已借阅的图书（借阅记录）
     * GET /api/borrow/borrowed/list
     * 
     * @param userId 用户ID
     * @param borrowStatus 借阅状态（可选）0-借阅中, 1-已还, 2-逾期未还
     * @return 借阅记录列表
     */
    @GetMapping("/borrowed/list")
    public Result getMyBorrowedBooks(
            @RequestParam Integer userId,
            @RequestParam(required = false) Integer borrowStatus) {
        
        List<BorrowRecordVO> records = borrowService.getMyBorrowedBooks(userId, borrowStatus);
        return Result.success(records);
    }

    /**
     * 归还图书
     * PUT /api/borrow/return
     * 
     * @param borrowId 借阅记录ID
     * @return 操作结果
     */
    @PutMapping("/return")
    public Result returnBook(@RequestParam Integer borrowId) {
        
        Boolean success = borrowService.returnBook(borrowId);
        if (success) {
            return Result.success("归还成功");
        } else {
            return Result.error(500, "归还失败，请检查记录状态");
        }
    }

    /**
     * 续借图书
     * PUT /api/borrow/renew
     * 
     * @param borrowId 借阅记录ID
     * @return 操作结果
     */
    @PutMapping("/renew")
    public Result renewBook(@RequestParam Integer borrowId) {
        
        Boolean success = borrowService.renewBook(borrowId);
        if (success) {
            return Result.success("续借成功");
        } else {
            return Result.error(500, "续借失败，可能已达到最大续借次数");
        }
    }

    /**
     * 查看图书逾期应缴纳罚款
     * GET /api/borrow/{borrow_id}/fine
     * 
     * @param borrowId 借阅记录ID
     * @return 罚金金额
     */
    @GetMapping("/{borrow_id}/fine")
    public Result getFineAmount(@PathVariable("borrow_id") Integer borrowId) {
        
        BigDecimal fineAmount = borrowService.getFineAmount(borrowId);
        if (fineAmount != null) {
            return Result.success(fineAmount);
        } else {
            return Result.error(404, "未找到借阅记录");
        }
    }
}
