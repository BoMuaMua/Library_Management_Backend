package com.library.borrow.service.impl;

import com.library.borrow.entity.vo.BorrowRecordVO;
import com.library.borrow.entity.BorrowingRecord;
import com.library.borrow.model.BookLocationModel;
import com.library.borrow.model.BorrowingRecordModel;
import com.library.borrow.service.BorrowService;
import com.library.user.model.UserModel;
import gaarason.database.appointment.Paginate;
import gaarason.database.contract.connection.GaarasonDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

/**
 * 借阅服务实现类
 */
@Service
public class BorrowServiceImpl implements BorrowService {

    @Autowired
    private BorrowingRecordModel borrowingRecordModel;

    @Autowired
    private BookLocationModel bookLocationModel;

    @Autowired
    private UserModel userModel;

    /**
     * 查询借阅记录列表（分页）
     * 支持通过书籍名称、用户昵称、学工号、电话号码、借阅时间、罚款状态查询，外联book_location表、user表和book表
     */
    @Override
    public Paginate<BorrowRecordVO> getBorrowRecords(Integer page, Integer limit,
                                                      String bookName, String userName,
                                                      String userCode, String phone,
                                                      String borrowTimeStart, String borrowTimeEnd,
                                                      Integer paymentStatus) {
        // 构建SQL查询
        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();

        sql.append("SELECT br.borrow_id, br.location_id, bl.book_id, br.user_id, ");
        sql.append("u.nickname, u.user_code, ");
        sql.append("b.title as book_title, ");
        sql.append("br.borrow_time, br.due_return_time, br.actual_return_time, ");
        sql.append("br.penalty, br.borrow_status, br.payment_status ");
        sql.append("FROM borrowing_record br ");
        sql.append("LEFT JOIN book_location bl ON br.location_id = bl.location_id ");
        sql.append("LEFT JOIN user u ON br.user_id = u.user_id ");
        sql.append("LEFT JOIN book b ON bl.book_id = b.book_id ");
        sql.append("WHERE 1=1 ");

        // 添加筛选条件
        // 添加书籍名称模糊查询
        if (StringUtils.hasText(bookName)) {
            sql.append("AND b.title LIKE ? ");
            params.add("%" + bookName + "%");
        }

        // 添加用户昵称模糊查询
        if (StringUtils.hasText(userName)) {
            sql.append("AND u.nickname LIKE ? ");
            params.add("%" + userName + "%");
        }

        // 添加工学号模糊查询
        if (StringUtils.hasText(userCode)) {
            sql.append("AND u.user_code LIKE ? ");
            params.add("%" + userCode + "%");
        }

        // 添加电话号码模糊查询
        if (StringUtils.hasText(phone)) {
            sql.append("AND u.phone LIKE ? ");
            params.add("%" + phone + "%");
        }

        if (StringUtils.hasText(borrowTimeStart)) {
            sql.append("AND br.borrow_time >= ? ");
            params.add(borrowTimeStart);
        }

        if (StringUtils.hasText(borrowTimeEnd)) {
            sql.append("AND br.borrow_time <= ? ");
            params.add(borrowTimeEnd);
        }

        if (paymentStatus != null) {
            sql.append("AND br.payment_status = ? ");
            params.add(paymentStatus);
        }

        // 添加排序：按借阅时间倒序，再按borrow_id倒序（二级排序保证稳定性）
        sql.append("ORDER BY br.borrow_time DESC, br.borrow_id DESC");

        // 执行分页查询
        GaarasonDataSource dataSource = borrowingRecordModel.getGaarasonDataSource();
        
        try {
            // 获取总数
            String countSql = "SELECT COUNT(*) FROM (" + sql.toString() + ") AS total";
            int total = executeCountQuery(dataSource, countSql, params);

            // 计算偏移量
            int offset = (page - 1) * limit;
            String pageSql = sql.toString() + " LIMIT ? OFFSET ?";
            List<Object> pageParams = new ArrayList<>(params);
            pageParams.add(limit);
            pageParams.add(offset);

            // 执行查询
            List<BorrowRecordVO> records = executeQuery(dataSource, pageSql, pageParams);

            // 构建分页对象
            Paginate<BorrowRecordVO> paginate = new Paginate<>(records, page, limit, (long) total);

            return paginate;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("查询借阅记录失败: " + e.getMessage());
        }
    }

    /**
     * 执行计数查询
     */
    private int executeCountQuery(GaarasonDataSource dataSource, String sql, List<Object> params) throws SQLException {
        try (PreparedStatement ps = dataSource.getConnection().prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        }
    }

    /**
     * 执行查询并映射结果
     */
    private List<BorrowRecordVO> executeQuery(GaarasonDataSource dataSource, String sql, List<Object> params) throws SQLException {
        List<BorrowRecordVO> result = new ArrayList<>();
        
        try (PreparedStatement ps = dataSource.getConnection().prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            
            try (ResultSet rs = ps.executeQuery()) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                
                while (rs.next()) {
                    BorrowRecordVO vo = new BorrowRecordVO();
                    vo.setBorrowId(rs.getInt("borrow_id"));
                    vo.setLocationId(rs.getInt("location_id"));
                    vo.setBookId(rs.getInt("book_id"));
                    vo.setBookTitle(rs.getString("book_title"));
                    vo.setUserId(rs.getInt("user_id"));
                    vo.setNickname(rs.getString("nickname"));
                    vo.setUserCode(rs.getString("user_code"));
                    
                    String borrowTimeStr = rs.getString("borrow_time");
                    if (borrowTimeStr != null && !borrowTimeStr.isEmpty()) {
                        vo.setBorrowTime(LocalDateTime.parse(borrowTimeStr, formatter));
                    }
                    
                    String dueReturnTimeStr = rs.getString("due_return_time");
                    if (dueReturnTimeStr != null && !dueReturnTimeStr.isEmpty()) {
                        vo.setDueReturnTime(LocalDateTime.parse(dueReturnTimeStr, formatter));
                    }
                    
                    String actualReturnTimeStr = rs.getString("actual_return_time");
                    if (actualReturnTimeStr != null && !actualReturnTimeStr.isEmpty()) {
                        vo.setActualReturnTime(LocalDateTime.parse(actualReturnTimeStr, formatter));
                    }
                    
                    vo.setPenalty(rs.getBigDecimal("penalty"));
                    vo.setBorrowStatus(rs.getInt("borrow_status"));
                    vo.setPaymentStatus(rs.getInt("payment_status"));
                    
                    result.add(vo);
                }
            }
        }
        
        return result;
    }

    /**
     * 缴费登记
     * 修改借阅记录的罚金支付状态为已支付，并更新罚金金额
     */
    @Override
    public Boolean payPenalty(Integer borrowId, BigDecimal penaltyAmount) {
        // 参数校验
        if (borrowId == null || penaltyAmount == null) {
            return false;
        }

        if (penaltyAmount.compareTo(BigDecimal.ZERO) < 0) {
            return false;
        }

        // 查询借阅记录是否存在
        var record = borrowingRecordModel.baseQuery()
                .where("borrow_id", borrowId)
                .limit(1)
                .first();

        if (record == null) {
            return false;
        }

        BorrowingRecord borrowingRecord = record.getEntity();

        // 检查是否为逾期记录且有未支付的罚金
        // borrowStatus: 2-逾期未还, paymentStatus: 1-未支付
        if (borrowingRecord.getBorrowStatus() != 2 || borrowingRecord.getPaymentStatus() != 1) {
            return false;
        }

        // 更新罚金支付状态为已支付（0），并设置罚金金额
        int rows = borrowingRecordModel.newQuery()
                .where("borrow_id", borrowId)
                .data("payment_status", 0)
                .data("penalty", penaltyAmount)
                .update();

        return rows > 0;
    }

    /**
     * 查询罚款记录列表（分页）
     * 仅查看借阅记录表中逾期记录，排序：未缴罚款排在已缴前面，二级排序按逾期时间先后
     */
    @Override
    public Paginate<BorrowRecordVO> getPenaltyRecords(Integer page, Integer limit,
                                                      String bookName, String userName,
                                                      String userCode, String phone,
                                                      Integer paymentStatus) {
        // 构建SQL查询
        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();

        sql.append("SELECT br.borrow_id, br.location_id, bl.book_id, br.user_id, ");
        sql.append("u.nickname, u.user_code, ");
        sql.append("b.title as book_title, ");
        sql.append("br.borrow_time, br.due_return_time, br.actual_return_time, ");
        sql.append("br.penalty, br.borrow_status, br.payment_status ");
        sql.append("FROM borrowing_record br ");
        sql.append("LEFT JOIN book_location bl ON br.location_id = bl.location_id ");
        sql.append("LEFT JOIN user u ON br.user_id = u.user_id ");
        sql.append("LEFT JOIN book b ON bl.book_id = b.book_id ");
        // 仅查询逾期记录：borrow_status=2
        sql.append("WHERE br.borrow_status = 2 ");

        // 添加筛选条件
        if (StringUtils.hasText(bookName)) {
            sql.append("AND b.title LIKE ? ");
            params.add("%" + bookName + "%");
        }

        if (StringUtils.hasText(userName)) {
            sql.append("AND u.nickname LIKE ? ");
            params.add("%" + userName + "%");
        }

        if (StringUtils.hasText(userCode)) {
            sql.append("AND u.user_code LIKE ? ");
            params.add("%" + userCode + "%");
        }

        if (StringUtils.hasText(phone)) {
            sql.append("AND u.phone LIKE ? ");
            params.add("%" + phone + "%");
        }

        if (paymentStatus != null) {
            sql.append("AND br.payment_status = ? ");
            params.add(paymentStatus);
        }

        // 排序：未缴罚款(payment_status=1)排在已缴(payment_status=0)前面，二级排序按逾期时间(due_return_time)先后
        sql.append("ORDER BY br.payment_status DESC, br.due_return_time ASC");

        // 执行分页查询
        GaarasonDataSource dataSource = borrowingRecordModel.getGaarasonDataSource();

        try {
            // 获取总数
            String countSql = "SELECT COUNT(*) FROM (" + sql.toString() + ") AS total";
            int total = executeCountQuery(dataSource, countSql, params);

            // 计算偏移量
            int offset = (page - 1) * limit;
            String pageSql = sql.toString() + " LIMIT ? OFFSET ?";
            List<Object> pageParams = new ArrayList<>(params);
            pageParams.add(limit);
            pageParams.add(offset);

            // 执行查询
            List<BorrowRecordVO> records = executeQuery(dataSource, pageSql, pageParams);

            // 构建分页对象
            Paginate<BorrowRecordVO> paginate = new Paginate<>(records, page, limit, (long) total);

            return paginate;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("查询罚款记录失败: " + e.getMessage());
        }
    }
}
