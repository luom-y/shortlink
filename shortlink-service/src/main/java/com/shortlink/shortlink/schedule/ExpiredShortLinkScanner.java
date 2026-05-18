package com.shortlink.shortlink.schedule;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.shortlink.shortlink.dao.entity.ShortLinkDO;
import com.shortlink.shortlink.dao.mapper.ShortLinkMapper;
import com.shortlink.shortlink.constants.RedisConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时扫描过期短链接，自动禁用并清除Redis缓存。
 * 每分钟执行一次。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExpiredShortLinkScanner {

    private final ShortLinkMapper shortLinkMapper;
    private final StringRedisTemplate stringRedisTemplate;

    @Scheduled(cron = "0 * * * * ?")
    public void scanExpired() {
        List<ShortLinkDO> expired = shortLinkMapper.selectList(
                Wrappers.lambdaQuery(ShortLinkDO.class)
                        .eq(ShortLinkDO::getStatus, 1)
                        .lt(ShortLinkDO::getExpireTime, LocalDateTime.now())
                        .isNotNull(ShortLinkDO::getExpireTime)
        );

        if (expired.isEmpty()) {
            return;
        }

        for (ShortLinkDO link : expired) {
            shortLinkMapper.update(null,
                    Wrappers.lambdaUpdate(ShortLinkDO.class)
                            .eq(ShortLinkDO::getId, link.getId())
                            .set(ShortLinkDO::getStatus, 0)
            );
            String cacheKey = RedisConstants.SHORTLINK_CACHE + link.getShortCode();
            stringRedisTemplate.delete(cacheKey);
            log.info("短链接已过期: shortCode={}, expireTime={}",
                    link.getShortCode(), link.getExpireTime());
        }

        log.info("过期扫描完成: {} 条链接已禁用", expired.size());
    }
}
