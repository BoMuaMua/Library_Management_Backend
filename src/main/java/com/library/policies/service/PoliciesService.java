package com.library.policies.service;

import com.library.policies.entity.vo.BorrowingPrivilegeVO;
import com.library.policies.entity.vo.OverdueFineVO;

import java.util.List;

/**
 * 借阅策略服务接口
 */
public interface PoliciesService {

    /**
     * 1.1 查看借阅权限设置（所有角色）
     * @return 所有角色的借阅权限列表
     */
    List<BorrowingPrivilegeVO> getBorrowingPrivileges();

    /**
     * 1.2 修改借阅权限
     * @param privilegeVO 要修改的权限信息（必须包含id）
     * @return 是否修改成功
     */
    boolean updateBorrowingPrivilege(BorrowingPrivilegeVO privilegeVO);

    /**
     * 1.3 新增借阅权限
     * @param privilegeVO 要新增的权限信息
     * @return 是否新增成功
     */
    boolean addBorrowingPrivilege(BorrowingPrivilegeVO privilegeVO);

    /**
     * 1.4 查看逾期罚金标准（所有角色）
     * @return 所有角色的逾期罚金标准列表
     */
    List<OverdueFineVO> getOverdueFines();

    /**
     * 1.5 修改逾期罚金标准
     * @param fineVO 要修改的罚金标准信息（必须包含id）
     * @return 是否修改成功
     */
    boolean updateOverdueFine(OverdueFineVO fineVO);
}
