package com.shortlink.admin.controller;

import com.shortlink.admin.dto.req.UserLoginReqDTO;
import com.shortlink.admin.dto.req.UserRefreshTokenReqDTO;
import com.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.shortlink.admin.dto.req.UserUpdateReqDTO;
import com.shortlink.admin.dto.resp.UserLoginRespDTO;
import com.shortlink.admin.service.UserService;
import com.shortlink.common.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 用户注册
     * @param requestParam
     * @return
     */
    @PostMapping("/api/v1/admin/users")
    public Result<Void> register(@Valid @RequestBody UserRegisterReqDTO requestParam) {
        userService.register(requestParam);
        return Result.success();
    }

    /**
     * 用户登录
     * @param requestParam
     * @return
     */
    @PostMapping("/api/v1/admin/users/login")
    public Result<UserLoginRespDTO> login(@Valid @RequestBody UserLoginReqDTO requestParam) {
        return Result.success(userService.login(requestParam));
    }

    /**
     * 用户登录token重置
     * @param requestParam
     * @return
     */
    @PostMapping("/api/v1/admin/users/refresh")
    public Result<UserLoginRespDTO> refresh(@Valid @RequestBody UserRefreshTokenReqDTO requestParam) {
        return Result.success(userService.refreshToken(requestParam));
    }

    /**
     * 用户退出
     */
    @PostMapping("/api/v1/admin/users/logout")
    public Result<Void> logout(@RequestHeader("Authorization") String authorization) {
        String token = authorization.replace("Bearer ", "");
        userService.logout(token);
        return Result.success();
    }

    /**
     * 用户修改
     * @param id
     * @param requestParam
     * @return
     */
    @PutMapping("/api/v1/admin/users/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody UserUpdateReqDTO requestParam) {
        userService.update(id, requestParam);
        return Result.success();
    }
}
