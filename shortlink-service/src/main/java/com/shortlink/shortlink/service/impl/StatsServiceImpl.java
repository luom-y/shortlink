package com.shortlink.shortlink.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shortlink.shortlink.dao.entity.ClickRecordDO;
import com.shortlink.shortlink.dao.mapper.ClickRecordMapper;
import com.shortlink.shortlink.dto.resp.ClickStatsRespDTO;
import com.shortlink.shortlink.service.StatsService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 统计服务实现（同步方案，无MQ）。
 * 每次短链接跳转时同步记录点击事件，按日聚合查询PV/UV。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final ClickRecordMapper clickRecordMapper;

    @Override
    public void recordClick(String shortCode, HttpServletRequest request) {
        String ip = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        String referer = request.getHeader("Referer");

        // 截断过长字段
        if (userAgent != null && userAgent.length() > 500) {
            userAgent = userAgent.substring(0, 500);
        }
        if (referer != null && referer.length() > 1000) {
            referer = referer.substring(0, 1000);
        }

        ClickRecordDO record = new ClickRecordDO();
        record.setShortCode(shortCode);
        record.setIp(ip);
        record.setUserAgent(userAgent);
        record.setReferer(referer);
        clickRecordMapper.insert(record);
    }

    @Override
    public List<ClickStatsRespDTO> getDailyStats(String shortCode, LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        // 查全量记录，在Java中按日期分组聚合
        List<ClickRecordDO> records = clickRecordMapper.selectList(
                new LambdaQueryWrapper<ClickRecordDO>()
                        .eq(ClickRecordDO::getShortCode, shortCode)
                        .ge(ClickRecordDO::getCreateTime, start)
                        .lt(ClickRecordDO::getCreateTime, end)
        );

        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Map<String, List<ClickRecordDO>> grouped = records.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getCreateTime().format(dateFmt)
                ));

        // 补全日期范围内每一天（含无数据的日期）
        List<ClickStatsRespDTO> result = new ArrayList<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            String dateKey = current.format(dateFmt);
            List<ClickRecordDO> dayRecords = grouped.getOrDefault(dateKey, List.of());

            long pv = dayRecords.size();
            long uv = dayRecords.stream()
                    .map(ClickRecordDO::getIp)
                    .filter(StrUtil::isNotBlank)
                    .distinct()
                    .count();

            result.add(ClickStatsRespDTO.builder()
                    .shortCode(shortCode)
                    .statsDate(dateKey)
                    .pv(pv)
                    .uv(uv)
                    .ipCount(uv)
                    .build());

            current = current.plusDays(1);
        }

        return result;
    }

    @Override
    public Long getTotalClicks(String shortCode) {
        return clickRecordMapper.selectCount(
                new LambdaQueryWrapper<ClickRecordDO>()
                        .eq(ClickRecordDO::getShortCode, shortCode)
        );
    }

    /** 获取客户端真实IP（支持反向代理） */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
