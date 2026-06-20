package com.library.policies.controller;

import com.library.auth.common.Result;
import com.library.policies.entity.vo.BorrowingPrivilegeVO;
import com.library.policies.entity.vo.OverdueFineVO;
import com.library.policies.service.PoliciesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 借阅策略控制器
 */
@RestController
@RequestMapping("/api/policies")
public class PoliciesController {

    @Autowired
    private PoliciesService policiesService;

    /**
     * 1.1 查看借阅权限设置（教师、学生、访客）
     * GET /api/policies/borrowing-privileges
     * 
     * @return 所有角色的借阅权限列表
     */
    @GetMapping("/borrowing-privileges")
    public Result getBorrowingPrivileges() {
        List<BorrowingPrivilegeVO> result = policiesService.getBorrowingPrivileges();
        return Result.success(result);
    }

    /**
     * 1.2 修改借阅权限
     * PUT /api/policies/borrowing-privileges
     * 
     * @param privilegeVO 要修改的权限信息（必须包含id）
     * @return 操作结果
     */
    @PutMapping("/borrowing-privileges")
    public Result updateBorrowingPrivilege(@RequestBody BorrowingPrivilegeVO privilegeVO) {
        boolean success = policiesService.updateBorrowingPrivilege(privilegeVO);
        return success ? Result.success("修改成功") : Result.error(400, "修改失败");
    }

    /**
     * 1.3 新增借阅权限
     * POST /api/policies/borrowing-privileges
     * 
     * @param privilegeVO 要新增的权限信息
     * @return 操作结果
     */
    @PostMapping("/borrowing-privileges")
    public Result addBorrowingPrivilege(@RequestBody BorrowingPrivilegeVO privilegeVO) {
        boolean success = policiesService.addBorrowingPrivilege(privilegeVO);
        return success ? Result.success("新增成功") : Result.error(400, "新增失败");
    }

    /**
     * 1.4 查看逾期罚金标准
     * GET /api/policies/overdue-fines
     * 
     * @return 所有角色的逾期罚金标准列表
     */
    @GetMapping("/overdue-fines")
    public Result getOverdueFines() {
        List<OverdueFineVO> result = policiesService.getOverdueFines();
        return Result.success(result);
    }

    /**
     * 1.5 修改逾期罚金标准
     * PUT /api/policies/overdue-fines
     * 
     * @param fineVO 要修改的罚金标准信息（必须包含id）
     * @return 操作结果
     */
    @PutMapping("/overdue-fines")
    public Result updateOverdueFine(@RequestBody OverdueFineVO fineVO) {
        boolean success = policiesService.updateOverdueFine(fineVO);
        return success ? Result.success("修改成功") : Result.error(400, "修改失败");
    }
}
