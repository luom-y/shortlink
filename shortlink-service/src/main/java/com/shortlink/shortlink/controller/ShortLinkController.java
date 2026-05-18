package com.shortlink.shortlink.controller;

import com.shortlink.common.result.Result;
import com.shortlink.shortlink.dto.req.ShortLinkCreateReqDTO;
import com.shortlink.shortlink.dto.req.ShortLinkUpdateReqDTO;
import com.shortlink.shortlink.dto.resp.ShortLinkRespDTO;
import com.shortlink.shortlink.service.ShortLinkService;
import com.shortlink.shortlink.service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@Tag(name = "Shortlink", description = "Short link CRUD and redirect")
public class ShortLinkController {

    private final ShortLinkService shortLinkService;
    private final StatsService statsService;

    /** 创建短链接（需要登录） */
    @PostMapping("/api/v1/shortlinks")
    @Operation(summary = "Create short link", description = "Generate a short code for a long URL")
    public Result<ShortLinkRespDTO> create(
            @Valid @RequestBody ShortLinkCreateReqDTO requestParam,
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId) {
        return Result.success(shortLinkService.create(requestParam, userId));
    }

    /** 短链接跳转：302重定向（无需登录），同时记录点击统计 */
    @GetMapping("/{shortCode}")
    @Operation(summary = "Redirect", description = "302 redirect to original URL (no auth required)")
    public ResponseEntity<Void> redirect(
            @Parameter(description = "Short code", example = "aBc123") @PathVariable String shortCode,
            HttpServletRequest request) {
        String originalUrl = shortLinkService.redirect(shortCode);
        statsService.recordClick(shortCode, request);
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(originalUrl));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    /** 查询短链接详情 */
    @GetMapping("/api/v1/shortlinks/{shortCode}")
    @Operation(summary = "Get short link details", description = "Query short link by short code")
    public Result<ShortLinkRespDTO> getByShortCode(
            @Parameter(description = "Short code") @PathVariable String shortCode) {
        return Result.success(shortLinkService.getByShortCode(shortCode));
    }

    /** 更新短链接（仅创建者可操作） */
    @PutMapping("/api/v1/shortlinks/{id}")
    @Operation(summary = "Update short link", description = "Update original URL, expiry, or status (owner only)")
    public Result<ShortLinkRespDTO> update(
            @Parameter(description = "Record ID") @PathVariable Long id,
            @Valid @RequestBody ShortLinkUpdateReqDTO requestParam,
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId) {
        return Result.success(shortLinkService.update(id, requestParam, userId));
    }

    /** 删除短链接（仅创建者可操作） */
    @DeleteMapping("/api/v1/shortlinks/{id}")
    @Operation(summary = "Delete short link", description = "Soft-delete a short link (owner only)")
    public Result<Void> delete(
            @Parameter(description = "Record ID") @PathVariable Long id,
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId) {
        shortLinkService.delete(id, userId);
        return Result.success();
    }
}
