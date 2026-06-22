package com.library.borrow.service.impl;

import com.library.borrow.entity.vo.BorrowRecordVO;
import com.library.borrow.entity.BorrowingRecord;
import com.library.borrow.model.BookLocationModel;
import com.library.borrow.model.BorrowingRecordModel;
import com.library.borrow.service.BorrowService;
import com.library.book.model.BookModel;
import com.library.user.model.UserModel;
import com.library.util.RedisUtils;
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
import java.util.concurrent.TimeUnit;

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

    @Autowired
    private BookModel bookModel;
    
    @Autowired
    private RedisUtils redisUtils;
    
    // Redis键前缀
    private static final String BORROW_RECORD_CACHE_PREFIX = "borrow:record:id:";
    private static final String MY_BORROW_CACHE_PREFIX = "borrow:my:user:";
    private static final String FINE_CACHE_PREFIX = "borrow:fine:id:";
    private static final long CACHE_EXPIRE_TIME = 30; // 缓存过期时间（分钟）


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

    /**
     * 借阅图书
     * 生成借阅记录，同时book表中的总借阅次数加一
     */
    @Override
    public Boolean borrowBook(Integer userId, Integer locationId) {
        // 参数校验
        if (userId == null || locationId == null) {
            return false;
        }

        // 检查用户是否存在
        var userRecord = userModel.baseQuery()
                .where("user_id", userId)
                .limit(1)
                .first();
        if (userRecord == null) {
            return false;
        }

        // 检查馆藏位置是否存在
        var locationRecord = bookLocationModel.baseQuery()
                .where("location_id", locationId)
                .limit(1)
                .first();
        if (locationRecord == null) {
            return false;
        }

        // 获取对应的book_id
        Integer bookId = locationRecord.getEntity().getBookId();

        // 创建借阅记录
        BorrowingRecord record = new BorrowingRecord();
        record.setUserId(userId);
        record.setLocationId(locationId);
        record.setBorrowTime(LocalDateTime.now());
        record.setDueReturnTime(LocalDateTime.now().plusDays(30)); // 默认30天借阅期
        record.setBorrowStatus(0); // 0-借阅中
        record.setPaymentStatus(0); // 0-无需支付/已支付
        record.setPenalty(BigDecimal.ZERO);

        Boolean success = borrowingRecordModel.newRecord().fillEntity(record).save();
        if (!success) {
            return false;
        }

        // 更新book表的总借阅次数加一
        // 先查询当前的总借阅次数
        var bookRecord = bookModel.baseQuery()
                .where("book_id", bookId)
                .limit(1)
                .first();
        
        if (bookRecord != null) {
            com.library.book.entity.Book book = bookRecord.getEntity();
            Long currentCount = book.getTotalBorrowingTime();
            if (currentCount == null) {
                currentCount = 0L;
            }
            
            // 更新总借阅次数
            bookModel.newQuery()
                    .where("book_id", bookId)
                    .data("total_borrowing_time", currentCount + 1)
                    .update();
            
            // 清除图书相关缓存
            redisUtils.delete("book:id:" + bookId);
        }

        return true;
    }

    /**
     * 查看自己已借阅的图书（借阅记录）
     * 可按状态筛选
     */
    @Override
    public java.util.List<BorrowRecordVO> getMyBorrowedBooks(Integer userId, Integer borrowStatus) {
        if (userId == null) {
            return new ArrayList<>();
        }

        // 构建缓存key
        String cacheKey = MY_BORROW_CACHE_PREFIX + userId + ":status:" + (borrowStatus != null ? borrowStatus : "all");
        
        // 先从Redis缓存中获取
        Object cached = redisUtils.get(cacheKey);
        if (cached instanceof List) {
            return (List<BorrowRecordVO>) cached;
        }

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
        sql.append("WHERE br.user_id = ? ");
        params.add(userId);

        // 添加状态筛选
        if (borrowStatus != null) {
            sql.append("AND br.borrow_status = ? ");
            params.add(borrowStatus);
        }

        // 按借阅时间倒序
        sql.append("ORDER BY br.borrow_time DESC");

        GaarasonDataSource dataSource = borrowingRecordModel.getGaarasonDataSource();

        try {
            List<BorrowRecordVO> result = executeQuery(dataSource, sql.toString(), params);
            
            // 存入Redis缓存
            redisUtils.save(cacheKey, result, CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
            
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("查询我的借阅记录失败: " + e.getMessage());
        }
    }

    /**
     * 归还图书
     */
    @Override
    public Boolean returnBook(Integer borrowId) {
        if (borrowId == null) {
            return false;
        }

        // 查询借阅记录
        var record = borrowingRecordModel.baseQuery()
                .where("borrow_id", borrowId)
                .limit(1)
                .first();

        if (record == null) {
            return false;
        }

        BorrowingRecord borrowingRecord = record.getEntity();

        // 检查是否已经归还
        if (borrowingRecord.getBorrowStatus() == 1) {
            return false; // 已经归还
        }

        // 计算逾期罚金（如果逾期）
        BigDecimal penalty = BigDecimal.ZERO;
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(borrowingRecord.getDueReturnTime())) {
            // 逾期天数
            long overdueDays = java.time.Duration.between(borrowingRecord.getDueReturnTime(), now).toDays();
            
            // 获取用户的角色代码，查询对应的罚金策略
            var userRecord = userModel.baseQuery()
                    .where("user_id", borrowingRecord.getUserId())
                    .limit(1)
                    .first();
            
            if (userRecord != null) {
                Integer roleCode = userRecord.getEntity().getRoleCode();
                
                // 查询该角色的罚金策略
                com.library.policies.model.BorrowStrategiesModel strategyModel = 
                        new com.library.policies.model.BorrowStrategiesModel();
                var strategyRecord = strategyModel.baseQuery()
                        .where("role_code", roleCode)
                        .limit(1)
                        .first();
                
                if (strategyRecord != null) {
                    var strategy = strategyRecord.getEntity();
                    penalty = strategy.getDailyPenalty().multiply(new BigDecimal(overdueDays));
                    
                    // 不超过最高罚金上限
                    if (penalty.compareTo(strategy.getMaxPenaltyLimit()) > 0) {
                        penalty = strategy.getMaxPenaltyLimit();
                    }
                }
            }
        }

        // 更新借阅记录
        int rows = borrowingRecordModel.newQuery()
                .where("borrow_id", borrowId)
                .data("actual_return_time", now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .data("borrow_status", 1) // 1-已还
                .data("penalty", penalty)
                .data("payment_status", penalty.compareTo(BigDecimal.ZERO) > 0 ? 1 : 0) // 有罚金则未支付
                .update();

        // 更新成功后，清除相关缓存
        if (rows > 0) {
            redisUtils.delete(BORROW_RECORD_CACHE_PREFIX + borrowId);
            redisUtils.delete(FINE_CACHE_PREFIX + borrowId);
            // 清除该用户的所有借阅记录缓存
            redisUtils.clearCache(MY_BORROW_CACHE_PREFIX + borrowingRecord.getUserId());
        }

        return rows > 0;
    }

    /**
     * 续借图书
     */
    @Override
    public Boolean renewBook(Integer borrowId) {
        if (borrowId == null) {
            return false;
        }

        // 查询借阅记录
        var record = borrowingRecordModel.baseQuery()
                .where("borrow_id", borrowId)
                .limit(1)
                .first();

        if (record == null) {
            return false;
        }

        BorrowingRecord borrowingRecord = record.getEntity();

        // 只能续借借阅中或逾期未还的记录
        if (borrowingRecord.getBorrowStatus() != 0 && borrowingRecord.getBorrowStatus() != 2) {
            return false;
        }


        // 查询续借次数表，检查是否已达到最大续借次数
        LocalDateTime newDueTime = borrowingRecord.getDueReturnTime().plusDays(15);

        int rows = borrowingRecordModel.newQuery()
                .where("borrow_id", borrowId)
                .data("due_return_time", newDueTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .update();

        // 更新成功后，清除相关缓存
        if (rows > 0) {
            redisUtils.delete(BORROW_RECORD_CACHE_PREFIX + borrowId);
            redisUtils.delete(FINE_CACHE_PREFIX + borrowId);
            // 清除该用户的所有借阅记录缓存
            redisUtils.clearCache(MY_BORROW_CACHE_PREFIX + borrowingRecord.getUserId());
        }

        return rows > 0;
    }

    /**
     * 查看图书逾期应缴纳罚款
     */
    @Override
    public java.math.BigDecimal getFineAmount(Integer borrowId) {
        if (borrowId == null) {
            return null;
        }

        // 先从Redis缓存中获取
        String cacheKey = FINE_CACHE_PREFIX + borrowId;
        Object cached = redisUtils.get(cacheKey);
        if (cached instanceof BigDecimal) {
            return (BigDecimal) cached;
        }

        // 查询借阅记录
        var record = borrowingRecordModel.baseQuery()
                .where("borrow_id", borrowId)
                .limit(1)
                .first();

        if (record == null) {
            return null;
        }

        BorrowingRecord borrowingRecord = record.getEntity();

        // 如果已经归还，返回记录的罚金
        if (borrowingRecord.getBorrowStatus() == 1) {
            BigDecimal penalty = borrowingRecord.getPenalty();
            // 存入缓存
            redisUtils.save(cacheKey, penalty, CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
            return penalty;
        }

        // 如枟正在借阅中且未逾期，返回0
        LocalDateTime now = LocalDateTime.now();
        if (!now.isAfter(borrowingRecord.getDueReturnTime())) {
            redisUtils.save(cacheKey, BigDecimal.ZERO, CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
            return BigDecimal.ZERO;
        }

        // 计算当前逾期罚金
        long overdueDays = java.time.Duration.between(borrowingRecord.getDueReturnTime(), now).toDays();

        // 获取用户角色的罚金策略
        var userRecord = userModel.baseQuery()
                .where("user_id", borrowingRecord.getUserId())
                .limit(1)
                .first();

        if (userRecord == null) {
            return null;
        }

        Integer roleCode = userRecord.getEntity().getRoleCode();

        try {
            com.library.policies.model.BorrowStrategiesModel strategyModel = 
                    new com.library.policies.model.BorrowStrategiesModel();
            var strategyRecord = strategyModel.baseQuery()
                    .where("role_code", roleCode)
                    .limit(1)
                    .first();

            if (strategyRecord == null) {
                return null;
            }

            var strategy = strategyRecord.getEntity();
            BigDecimal penalty = strategy.getDailyPenalty().multiply(new BigDecimal(overdueDays));

            // 不超过最高罚金上限
            if (penalty.compareTo(strategy.getMaxPenaltyLimit()) > 0) {
                penalty = strategy.getMaxPenaltyLimit();
            }

            // 存入缓存（短期缓存，因为罚金会随时间变化）
            redisUtils.save(cacheKey, penalty, 5, TimeUnit.MINUTES);

            return penalty;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
