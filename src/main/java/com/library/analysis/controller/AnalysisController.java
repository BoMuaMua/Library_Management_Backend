package com.library.analysis.controller;

import com.library.auth.common.Result;
import com.library.analysis.entity.vo.BookTopVO;
import com.library.analysis.entity.vo.BorrowFrequencyVO;
import com.library.analysis.entity.vo.CategoryDistributionVO;
import com.library.analysis.entity.vo.InventoryAnalysisVO;
import com.library.analysis.service.AnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 数据分析控制器
 */
@RestController
@RequestMapping("/api")
public class AnalysisController {

    @Autowired
    private AnalysisService analysisService;

    /**
     * 1. 查看图书借阅排行榜
     * GET /api/books/top
     * 
     * @param limit 返回数量（默认10）
     * @return 按借阅次数降序排列的图书列表
     */
    @GetMapping("/books/top")
    public Result getBookBorrowRanking(
            @RequestParam(defaultValue = "10") Integer limit) {
        
        List<BookTopVO> result = analysisService.getBookBorrowRanking(limit);
        return Result.success(result);
    }

    /**
     * 2. 查看借阅频次分析图表
     * GET /api/analytics/borrow-frequencies
     * 
     * @param period 时间周期：day-日, week-周, month-月（默认day）
     * @param startDate 开始日期（可选），格式：yyyy-MM-dd
     * @param endDate 结束日期（可选），格式：yyyy-MM-dd
     * @return 按时间段统计的借阅频次数据
     */
    @GetMapping("/analytics/borrow-frequencies")
    public Result getBorrowFrequencies(
            @RequestParam(defaultValue = "day") String period,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        List<BorrowFrequencyVO> result = analysisService.getBorrowFrequencies(period, startDate, endDate);
        return Result.success(result);
    }

    /**
     * 3. 查看图书类型分布
     * GET /api/analytics/book-categories
     * 
     * @return 按分类统计的图书数量
     */
    @GetMapping("/analytics/book-categories")
    public Result getCategoryDistribution() {
        
        List<CategoryDistributionVO> result = analysisService.getCategoryDistribution();
        return Result.success(result);
    }

    /**
     * 4. 查看库存分析
     * GET /api/analytics/inventories
     * 
     * @return 库存统计数据
     */
    @GetMapping("/analytics/inventories")
    public Result getInventoryAnalysis() {
        
        InventoryAnalysisVO result = analysisService.getInventoryAnalysis();
        return Result.success(result);
    }
}
