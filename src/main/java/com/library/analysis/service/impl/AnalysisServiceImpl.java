package com.library.analysis.service.impl;

import com.library.analysis.entity.vo.BookTopVO;
import com.library.analysis.entity.vo.BorrowFrequencyVO;
import com.library.analysis.entity.vo.CategoryDistributionVO;
import com.library.analysis.entity.vo.InventoryAnalysisVO;
import com.library.analysis.service.AnalysisService;
import com.library.book.model.BookModel;
import com.library.borrow.model.BorrowingRecordModel;
import gaarason.database.contract.connection.GaarasonDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据分析服务实现类
 */
@Service
public class AnalysisServiceImpl implements AnalysisService {

    @Autowired
    private BorrowingRecordModel borrowingRecordModel;

    @Autowired
    private BookModel bookModel;

    /**
     * 1. 查看图书借阅排行榜
     * 统计每本书的借阅次数，按借阅次数降序排列
     */
    @Override
    public List<BookTopVO> getBookBorrowRanking(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10; // 默认返回前10本
        }

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT b.book_id, b.title, b.author, b.classification, ");
        sql.append("COUNT(br.borrow_id) as borrow_count ");
        sql.append("FROM book b ");
        sql.append("LEFT JOIN book_location bl ON b.book_id = bl.book_id ");
        sql.append("LEFT JOIN borrowing_record br ON bl.location_id = br.location_id ");
        sql.append("GROUP BY b.book_id, b.title, b.author, b.classification ");
        sql.append("ORDER BY borrow_count DESC, b.book_id ASC ");
        sql.append("LIMIT ?");

        GaarasonDataSource dataSource = bookModel.getGaarasonDataSource();

        try {
            return executeQuery(dataSource, sql.toString(), new Object[]{limit}, rs -> {
                List<BookTopVO> result = new ArrayList<>();
                while (rs.next()) {
                    BookTopVO vo = new BookTopVO();
                    vo.setBookId(rs.getInt("book_id"));
                    vo.setTitle(rs.getString("title"));
                    vo.setAuthor(rs.getString("author"));
                    vo.setClassification(rs.getString("classification"));
                    vo.setBorrowCount(rs.getLong("borrow_count"));
                    result.add(vo);
                }
                return result;
            });
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("查询图书借阅排行榜失败: " + e.getMessage());
        }
    }

    /**
     * 2. 查看借阅频次分析图表
     * 按时间段统计借阅数量
     */
    @Override
    public List<BorrowFrequencyVO> getBorrowFrequencies(String period, String startDate, String endDate) {
        if (!StringUtils.hasText(period)) {
            period = "day"; // 默认按日统计
        }

        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();

        sql.append("SELECT ");

        // 根据周期选择不同的时间格式化函数
        switch (period.toLowerCase()) {
            case "day":
                sql.append("DATE_FORMAT(br.borrow_time, '%Y-%m-%d') as time_period, ");
                break;
            case "week":
                sql.append("CONCAT(YEARWEEK(br.borrow_time), '周') as time_period, ");
                break;
            case "month":
                sql.append("DATE_FORMAT(br.borrow_time, '%Y-%m') as time_period, ");
                break;
            default:
                sql.append("DATE_FORMAT(br.borrow_time, '%Y-%m-%d') as time_period, ");
        }

        sql.append("COUNT(*) as count ");
        sql.append("FROM borrowing_record br ");
        sql.append("WHERE 1=1 ");

        // 添加日期范围筛选
        if (StringUtils.hasText(startDate)) {
            sql.append("AND br.borrow_time >= ? ");
            params.add(startDate + " 00:00:00");
        }

        if (StringUtils.hasText(endDate)) {
            sql.append("AND br.borrow_time <= ? ");
            params.add(endDate + " 23:59:59");
        }

        sql.append("GROUP BY time_period ");
        sql.append("ORDER BY time_period ASC");

        GaarasonDataSource dataSource = borrowingRecordModel.getGaarasonDataSource();

        try {
            return executeQuery(dataSource, sql.toString(), params.toArray(), rs -> {
                List<BorrowFrequencyVO> result = new ArrayList<>();
                while (rs.next()) {
                    BorrowFrequencyVO vo = new BorrowFrequencyVO();
                    vo.setTimePeriod(rs.getString("time_period"));
                    vo.setCount(rs.getLong("count"));
                    result.add(vo);
                }
                return result;
            });
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("查询借阅频次分析失败: " + e.getMessage());
        }
    }

    /**
     * 3. 查看图书类型分布
     * 按分类统计图书数量
     */
    @Override
    public List<CategoryDistributionVO> getCategoryDistribution() {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT classification as category, COUNT(*) as count ");
        sql.append("FROM book ");
        sql.append("GROUP BY classification ");
        sql.append("ORDER BY count DESC, classification ASC");

        GaarasonDataSource dataSource = bookModel.getGaarasonDataSource();

        try {
            return executeQuery(dataSource, sql.toString(), new Object[]{}, rs -> {
                List<CategoryDistributionVO> result = new ArrayList<>();
                while (rs.next()) {
                    CategoryDistributionVO vo = new CategoryDistributionVO();
                    vo.setCategory(rs.getString("category"));
                    vo.setCount(rs.getLong("count"));
                    result.add(vo);
                }
                return result;
            });
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("查询图书类型分布失败: " + e.getMessage());
        }
    }

    /**
     * 4. 查看库存分析
     * 统计库存相关数据
     */
    @Override
    public InventoryAnalysisVO getInventoryAnalysis() {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        sql.append("COUNT(DISTINCT b.book_id) as total_book_types, ");
        sql.append("SUM(b.inventory) as total_books, ");
        sql.append("(SELECT COUNT(*) FROM borrowing_record WHERE borrow_status IN (0, 2)) as borrowed_books, ");
        sql.append("SUM(CASE WHEN b.inventory <= 5 THEN 1 ELSE 0 END) as low_stock_count, ");
        sql.append("SUM(CASE WHEN b.inventory = 0 THEN 1 ELSE 0 END) as zero_stock_count ");
        sql.append("FROM book b");

        GaarasonDataSource dataSource = bookModel.getGaarasonDataSource();

        try {
            List<InventoryAnalysisVO> results = executeQuery(dataSource, sql.toString(), new Object[]{}, rs -> {
                List<InventoryAnalysisVO> result = new ArrayList<>();
                InventoryAnalysisVO vo = new InventoryAnalysisVO();
                if (rs.next()) {
                    vo.setTotalBookTypes(rs.getLong("total_book_types"));
                    vo.setTotalBooks(rs.getObject("total_books") != null ? rs.getLong("total_books") : 0L);
                    vo.setBorrowedBooks(rs.getLong("borrowed_books"));

                    // 计算在库册数
                    long totalBooksValue = vo.getTotalBooks() != null ? vo.getTotalBooks() : 0L;
                    long borrowedBooksValue = vo.getBorrowedBooks() != null ? vo.getBorrowedBooks() : 0L;
                    vo.setAvailableBooks(totalBooksValue - borrowedBooksValue);

                    // 计算库存利用率
                    if (totalBooksValue > 0) {
                        BigDecimal utilizationRate = new BigDecimal(borrowedBooksValue)
                                .divide(new BigDecimal(totalBooksValue), 4, RoundingMode.HALF_UP)
                                .multiply(new BigDecimal(100));
                        vo.setUtilizationRate(utilizationRate.setScale(2, RoundingMode.HALF_UP));
                    } else {
                        vo.setUtilizationRate(BigDecimal.ZERO);
                    }

                    vo.setLowStockCount(rs.getLong("low_stock_count"));
                    vo.setZeroStockCount(rs.getLong("zero_stock_count"));
                }
                result.add(vo);
                return result;
            });
            return results.isEmpty() ? new InventoryAnalysisVO() : results.get(0);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("查询库存分析失败: " + e.getMessage());
        }
    }

    /**
     * 通用查询执行方法
     */
    private <T> List<T> executeQuery(GaarasonDataSource dataSource, String sql, Object[] params, ResultSetMapper<T> mapper) throws SQLException {
        try (PreparedStatement ps = dataSource.getConnection().prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return mapper.map(rs);
            }
        }
    }

    /**
     * ResultSet映射器接口
     */
    @FunctionalInterface
    private interface ResultSetMapper<T> {
        List<T> map(ResultSet rs) throws SQLException;
    }
}
