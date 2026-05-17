package com.shortlink.admin.controller;


import com.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.shortlink.admin.dto.req.UserUpdateReqDTO;
import com.shortlink.admin.service.UserService;
import com.shortlink.common.result.Result;
import com.shortlink.common.result.Results;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

public class UserController {

    private UserService userService;
    /**
     * 注册用户
     */
    @PostMapping("/api/v1/admin/users")
    public Result<Void> register(@RequestBody UserRegisterReqDTO requestParam) {
        userService.register(requestParam);
        return Results.success();
    }

    /**
     * 修改用户
     */
    @PutMapping("/api/v1/admin/users/{id}")
    public Result<Void> update( @PathVariable Long id ,@RequestBody UserUpdateReqDTO requestParam) {
        userService.update(id,requestParam);
        return Results.success();
    }

    /**
     *  用户登录
     */


}
