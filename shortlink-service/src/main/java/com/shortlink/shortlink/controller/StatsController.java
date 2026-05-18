package com.shortlink.shortlink.controller;

import com.shortlink.common.result.Result;
import com.shortlink.shortlink.dto.resp.ClickStatsRespDTO;
import com.shortlink.shortlink.service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/stats")
@RequiredArgsConstructor
@Tag(name = "Statistics", description = "Click statistics query")
public class StatsController {

    private final StatsService statsService;

    @GetMapping("/daily/{shortCode}")
    @Operation(summary = "Daily stats", description = "Get PV/UV per day for a short code in date range")
    public Result<List<ClickStatsRespDTO>> getDailyStats(
            @Parameter(description = "Short code") @PathVariable String shortCode,
            @Parameter(description = "Start date (inclusive)", example = "2026-05-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (inclusive)", example = "2026-05-18")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(statsService.getDailyStats(shortCode, startDate, endDate));
    }

    @GetMapping("/total/{shortCode}")
    @Operation(summary = "Total clicks", description = "Get total click count for a short code")
    public Result<Long> getTotalClicks(
            @Parameter(description = "Short code") @PathVariable String shortCode) {
        return Result.success(statsService.getTotalClicks(shortCode));
    }
}
