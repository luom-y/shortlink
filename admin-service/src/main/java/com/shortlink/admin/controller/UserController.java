package com.shortlink.admin.controller;

import com.shortlink.admin.common.convention.result.Result;
import com.shortlink.admin.common.convention.result.Results;
import com.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.shortlink.admin.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public class UserController {
    private UserService userService;
    /**
     * 注册用户
     */
    @PostMapping("/api/short-link/admin/v1/user")
    public Result<Void> register(@RequestBody UserRegisterReqDTO requestParam) {
        userService.register(requestParam);
        return Results.success();
    }
}
