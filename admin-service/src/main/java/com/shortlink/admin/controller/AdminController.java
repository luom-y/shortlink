package com.shortlink.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shortlink.admin.dao.entity.ShortLinkDO;
import com.shortlink.admin.dao.entity.UserDO;
import com.shortlink.admin.service.AdminService;
import com.shortlink.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 后台管理接口：用户管理、短链接管理（仅管理员可操作）。
 */
@RestController
@RequestMapping("/api/v1/admin/manage")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    /** 分页查询所有用户 */
    @GetMapping("/users")
    public Result<IPage<UserDO>> listUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(adminService.listUsers(page, size));
    }

    /** 切换用户启用/禁用状态 */
    @PutMapping("/users/{id}/status")
    public Result<Void> toggleUserStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        adminService.toggleUserStatus(id, status);
        return Result.success();
    }

    /** 分页查询所有短链接 */
    @GetMapping("/shortlinks")
    public Result<IPage<ShortLinkDO>> listShortLinks(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String shortCode) {
        return Result.success(adminService.listShortLinks(page, size, shortCode));
    }

    /** 切换短链接启用/禁用状态 */
    @PutMapping("/shortlinks/{id}/status")
    public Result<Void> toggleShortLinkStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        adminService.toggleShortLinkStatus(id, status);
        return Result.success();
    }

    /** 管理员强制删除短链接 */
    @DeleteMapping("/shortlinks/{id}")
    public Result<Void> forceDeleteShortLink(@PathVariable Long id) {
        adminService.forceDeleteShortLink(id);
        return Result.success();
    }
}
