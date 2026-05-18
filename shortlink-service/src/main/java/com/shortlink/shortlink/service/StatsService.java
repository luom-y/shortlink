package com.shortlink.shortlink.service;

import com.shortlink.shortlink.dto.resp.ClickStatsRespDTO;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDate;
import java.util.List;

public interface StatsService {

    /** Record a click event (called during redirect) */
    void recordClick(String shortCode, HttpServletRequest request);

    /** Query daily stats for a short code in date range */
    List<ClickStatsRespDTO> getDailyStats(String shortCode, LocalDate startDate, LocalDate endDate);

    /** Get total clicks for a short code */
    Long getTotalClicks(String shortCode);
}
