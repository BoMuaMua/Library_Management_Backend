package com.library.analysis.service;

import com.library.analysis.entity.vo.BookTopVO;
import com.library.analysis.entity.vo.BorrowFrequencyVO;
import com.library.analysis.entity.vo.CategoryDistributionVO;
import com.library.analysis.entity.vo.InventoryAnalysisVO;

import java.util.List;

/**
 * 数据分析服务接口
 */
public interface AnalysisService {

    /**
     * 1. 查看图书借阅排行榜
     * @param limit 返回数量，默认10
     * @return 按借阅次数降序排列的图书列表
     */
    List<BookTopVO> getBookBorrowRanking(Integer limit);

    /**
     * 2. 查看借阅频次分析图表
     * @param period 时间周期：day-日, week-周, month-月
     * @param startDate 开始日期（可选），格式：yyyy-MM-dd
     * @param endDate 结束日期（可选），格式：yyyy-MM-dd
     * @return 按时间段统计的借阅频次数据
     */
    List<BorrowFrequencyVO> getBorrowFrequencies(String period, String startDate, String endDate);

    /**
     * 3. 查看图书类型分布
     * @return 按分类统计的图书数量
     */
    List<CategoryDistributionVO> getCategoryDistribution();

    /**
     * 4. 查看库存分析
     * @return 库存统计数据
     */
    InventoryAnalysisVO getInventoryAnalysis();
}
