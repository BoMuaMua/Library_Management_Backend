package com.library.policies.service.impl;

import com.library.borrow.entity.BorrowStrategies;
import com.library.policies.entity.vo.BorrowingPrivilegeVO;
import com.library.policies.entity.vo.OverdueFineVO;
import com.library.policies.model.BorrowStrategiesModel;
import com.library.policies.service.PoliciesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 借阅策略服务实现类
 */
@Service
public class PoliciesServiceImpl implements PoliciesService {

    @Autowired
    private BorrowStrategiesModel borrowStrategiesModel;

    /**
     * 1.1 查看借阅权限设置（所有角色）
     */
    @Override
    public List<BorrowingPrivilegeVO> getBorrowingPrivileges() {
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
        return rows > 0;
    }

    /**
     * 1.4 查看逾期罚金标准（所有角色）
     */
    @Override
    public List<OverdueFineVO> getOverdueFines() {
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

        return rows > 0;
    }
}
