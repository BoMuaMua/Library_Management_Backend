package com.library.policies.service.impl;

import com.library.borrow.entity.BorrowStrategies;
import com.library.policies.entity.vo.BorrowingPrivilegeVO;
import com.library.policies.entity.vo.OverdueFineVO;
import com.library.policies.model.BorrowStrategiesModel;
import com.library.policies.service.PoliciesService;
import com.library.util.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 借阅策略服务实现类
 */
@Service
public class PoliciesServiceImpl implements PoliciesService {

    @Autowired
    private BorrowStrategiesModel borrowStrategiesModel;

    @Autowired
    private RedisUtils redisUtils;

    // Redis键前缀
    private static final String PRIVILEGES_CACHE_KEY = "policies:privileges:all";
    private static final String FINES_CACHE_KEY = "policies:fines:all";
    private static final String STRATEGY_CACHE_PREFIX = "policies:strategy:id:";
    private static final long CACHE_EXPIRE_TIME = 30; // 缓存过期时间（分钟）

    /**
     * 1.1 查看借阅权限设置（所有角色）
     */
    @Override
    public List<BorrowingPrivilegeVO> getBorrowingPrivileges() {
        // 先从Redis缓存中获取
        Object cached = redisUtils.get(PRIVILEGES_CACHE_KEY);
        if (cached instanceof List) {
            return (List<BorrowingPrivilegeVO>) cached;
        }

        // 缓存未命中，从数据库查询
        var records = borrowStrategiesModel.baseQuery().get();

        List<BorrowingPrivilegeVO> result = new ArrayList<>();
        if (records != null && !records.isEmpty()) {
            for (var record : records) {
                BorrowStrategies entity = record.getEntity();
                BorrowingPrivilegeVO vo = new BorrowingPrivilegeVO();
                vo.setId(entity.getId());
                vo.setRoleCode(entity.getRoleCode());
                vo.setMaxBooks(entity.getMaxBooks());
                vo.setMaxDays(entity.getMaxDays());
                vo.setRenewTimes(entity.getRenewTimes());
                vo.setDailyLimit(entity.getDailyLimit());
                vo.setDailyPenalty(entity.getDailyPenalty());
                vo.setMaxPenaltyLimit(entity.getMaxPenaltyLimit());
                result.add(vo);
            }
        }

        // 存入Redis缓存
        redisUtils.save(PRIVILEGES_CACHE_KEY, result, CACHE_EXPIRE_TIME, TimeUnit.MINUTES);

        return result;
    }

    /**
     * 1.2 修改借阅权限
     */
    @Override
    public boolean updateBorrowingPrivilege(BorrowingPrivilegeVO privilegeVO) {
        if (privilegeVO.getId() == null) {
            return false;
        }

        var existingRecord = borrowStrategiesModel.baseQuery()
                .where("id", privilegeVO.getId())
                .limit(1)
                .first();

        if (existingRecord == null) {
            return false;
        }

        int rows = borrowStrategiesModel.newQuery()
                .where("id", privilegeVO.getId())
                .data("role_code", privilegeVO.getRoleCode())
                .data("max_books", privilegeVO.getMaxBooks())
                .data("max_days", privilegeVO.getMaxDays())
                .data("renew_times", privilegeVO.getRenewTimes())
                .data("daily_limit", privilegeVO.getDailyLimit())
                .data("daily_penalty", privilegeVO.getDailyPenalty())
                .data("max_penalty_limit", privilegeVO.getMaxPenaltyLimit())
                .update();

        // 更新成功后，清除相关缓存
        if (rows > 0) {
            redisUtils.delete(PRIVILEGES_CACHE_KEY);
            redisUtils.delete(FINES_CACHE_KEY);
            redisUtils.delete(STRATEGY_CACHE_PREFIX + privilegeVO.getId());
        }

        return rows > 0;
    }

    /**
     * 1.3 新增借阅权限
     */
    @Override
    public boolean addBorrowingPrivilege(BorrowingPrivilegeVO privilegeVO) {
        BorrowStrategies entity = new BorrowStrategies();
        entity.setRoleCode(privilegeVO.getRoleCode());
        entity.setMaxBooks(privilegeVO.getMaxBooks());
        entity.setMaxDays(privilegeVO.getMaxDays());
        entity.setRenewTimes(privilegeVO.getRenewTimes());
        entity.setDailyLimit(privilegeVO.getDailyLimit());
        entity.setDailyPenalty(privilegeVO.getDailyPenalty());
        entity.setMaxPenaltyLimit(privilegeVO.getMaxPenaltyLimit());

        int rows = borrowStrategiesModel.newQuery().data(entity).insert();
        
        // 新增成功后，清除相关缓存
        if (rows > 0) {
            redisUtils.delete(PRIVILEGES_CACHE_KEY);
            redisUtils.delete(FINES_CACHE_KEY);
        }
        
        return rows > 0;
    }

    /**
     * 1.4 查看逾期罚金标准（所有角色）
     */
    @Override
    public List<OverdueFineVO> getOverdueFines() {
        // 先从Redis缓存中获取
        Object cached = redisUtils.get(FINES_CACHE_KEY);
        if (cached instanceof List) {
            return (List<OverdueFineVO>) cached;
        }

        // 缓存未命中，从数据库查询
        var records = borrowStrategiesModel.baseQuery().get();

        List<OverdueFineVO> result = new ArrayList<>();
        if (records != null && !records.isEmpty()) {
            for (var record : records) {
                BorrowStrategies entity = record.getEntity();
                OverdueFineVO vo = new OverdueFineVO();
                vo.setId(entity.getId());
                vo.setRoleCode(entity.getRoleCode());
                vo.setDailyPenalty(entity.getDailyPenalty());
                vo.setMaxPenaltyLimit(entity.getMaxPenaltyLimit());
                result.add(vo);
            }
        }

        // 存入Redis缓存
        redisUtils.save(FINES_CACHE_KEY, result, CACHE_EXPIRE_TIME, TimeUnit.MINUTES);

        return result;
    }

    /**
     * 1.5 修改逾期罚金标准
     */
    @Override
    public boolean updateOverdueFine(OverdueFineVO fineVO) {
        if (fineVO.getId() == null) {
            return false;
        }

        var existingRecord = borrowStrategiesModel.baseQuery()
                .where("id", fineVO.getId())
                .limit(1)
                .first();

        if (existingRecord == null) {
            return false;
        }

        int rows = borrowStrategiesModel.newQuery()
                .where("id", fineVO.getId())
                .data("daily_penalty", fineVO.getDailyPenalty())
                .data("max_penalty_limit", fineVO.getMaxPenaltyLimit())
                .update();

        // 更新成功后，清除相关缓存
        if (rows > 0) {
            redisUtils.delete(PRIVILEGES_CACHE_KEY);
            redisUtils.delete(FINES_CACHE_KEY);
            redisUtils.delete(STRATEGY_CACHE_PREFIX + fineVO.getId());
        }

        return rows > 0;
    }
}
