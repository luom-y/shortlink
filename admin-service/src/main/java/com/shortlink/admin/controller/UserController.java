package com.shortlink.admin.controller;

import com.shortlink.admin.dto.req.UserLoginReqDTO;
import com.shortlink.admin.dto.req.UserRefreshTokenReqDTO;
import com.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.shortlink.admin.dto.req.UserUpdateReqDTO;
import com.shortlink.admin.dto.resp.UserLoginRespDTO;
import com.shortlink.admin.service.UserService;
import com.shortlink.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "User", description = "User authentication and management")
public class UserController {

    private final UserService userService;

    @PostMapping("/api/v1/admin/users")
    @Operation(summary = "Register", description = "Create a new user account (no auth required)")
    public Result<Void> register(@Valid @RequestBody UserRegisterReqDTO requestParam) {
        userService.register(requestParam);
        return Result.success();
    }

    @PostMapping("/api/v1/admin/users/login")
    @Operation(summary = "Login", description = "Login and get access token + refresh token")
    public Result<UserLoginRespDTO> login(@Valid @RequestBody UserLoginReqDTO requestParam) {
        return Result.success(userService.login(requestParam));
    }

    @PostMapping("/api/v1/admin/users/refresh")
    @Operation(summary = "Refresh token", description = "Refresh access token using refresh token")
    public Result<UserLoginRespDTO> refresh(@Valid @RequestBody UserRefreshTokenReqDTO requestParam) {
        return Result.success(userService.refreshToken(requestParam));
    }

    @PostMapping("/api/v1/admin/users/logout")
    @Operation(summary = "Logout", description = "Invalidate current access token")
    public Result<Void> logout(@Parameter(hidden = true) @RequestHeader("Authorization") String authorization) {
        String token = authorization.replace("Bearer ", "");
        userService.logout(token);
        return Result.success();
    }

    @PutMapping("/api/v1/admin/users/{id}")
    @Operation(summary = "Update user", description = "Update user profile (partial update)")
    public Result<Void> update(
            @Parameter(description = "User ID") @PathVariable Long id,
            @Valid @RequestBody UserUpdateReqDTO requestParam) {
        userService.update(id, requestParam);
        return Result.success();
    }
}
