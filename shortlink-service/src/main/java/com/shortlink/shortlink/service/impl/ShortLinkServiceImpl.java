package com.shortlink.shortlink.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shortlink.common.exception.BusinessException;
import com.shortlink.common.result.ResultCode;
import com.shortlink.common.utils.Base62Codec;
import com.shortlink.common.utils.SnowflakeIdGenerator;
import com.shortlink.common.utils.UrlValidator;
import com.shortlink.shortlink.constants.RedisConstants;
import com.shortlink.shortlink.dao.entity.ShortLinkDO;
import com.shortlink.shortlink.dao.mapper.ShortLinkMapper;
import com.shortlink.shortlink.dto.req.ShortLinkCreateReqDTO;
import com.shortlink.shortlink.dto.req.ShortLinkUpdateReqDTO;
import com.shortlink.shortlink.dto.resp.ShortLinkRespDTO;
import com.shortlink.shortlink.service.ShortLinkService;
import com.shortlink.shortlink.utils.BloomFilterHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static com.shortlink.shortlink.constants.RedisConstants.NULL_PLACEHOLDER;
import static com.shortlink.shortlink.constants.RedisConstants.SHORTLINK_CACHE;

/**
 * 短链接核心服务实现。
 * 核心流程：
 * 1. 创建：雪花ID -> Base62编码 -> 布隆预判 -> DB写入 -> Redis缓存
 * 2. 跳转：布隆预判 -> Redis缓存 -> DB回源 -> 302重定向
 * 3. 缓存策略：读优先查Redis、空值缓存防穿透、过期TTL对齐
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements ShortLinkService {

    private final StringRedisTemplate stringRedisTemplate;
    private final BloomFilterHelper bloomFilterHelper;
    private final SnowflakeIdGenerator snowflakeIdGenerator = SnowflakeIdGenerator.getInstance();

    /** 短链接域名（生产环境应配置为真实域名） */
    private static final String SHORT_DOMAIN = "http://localhost:5000";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShortLinkRespDTO create(ShortLinkCreateReqDTO requestParam, Long userId) {
        String originalUrl = UrlValidator.normalize(requestParam.getOriginalUrl());
        if (!UrlValidator.isValid(originalUrl)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "Invalid URL format");
        }

        // 生成唯一短码：雪花ID -> Base62，布隆+DB双重校验防冲突
        String shortCode;
        do {
            long snowflakeId = snowflakeIdGenerator.nextId();
            shortCode = Base62Codec.encode(snowflakeId);
        } while (bloomFilterHelper.mightContain(shortCode) && checkDbExists(shortCode));

        ShortLinkDO shortLinkDO = new ShortLinkDO();
        shortLinkDO.setShortCode(shortCode);
        shortLinkDO.setOriginalUrl(originalUrl);
        shortLinkDO.setUserId(userId);
        shortLinkDO.setExpireTime(requestParam.getExpireTime());
        shortLinkDO.setTotalClicks(0L);
        shortLinkDO.setStatus(1);
        baseMapper.insert(shortLinkDO);

        // 写入Redis缓存 + 布隆过滤器
        cacheUrl(shortCode, originalUrl, requestParam.getExpireTime());
        bloomFilterHelper.add(shortCode);

        log.info("短链接创建成功: shortCode={}, originalUrl={}, userId={}", shortCode, originalUrl, userId);
        return toRespDTO(shortLinkDO);
    }

    @Override
    public String redirect(String shortCode) {
        if (StrUtil.isBlank(shortCode)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "Short code cannot be empty");
        }

        // 布隆过滤器快速拦截不存在的短码
        if (!bloomFilterHelper.mightContain(shortCode)) {
            throw new BusinessException(ResultCode.SHORTLINK_NOT_FOUND);
        }

        // 一级缓存：Redis
        String cacheKey = SHORTLINK_CACHE + shortCode;
        String cachedUrl = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cachedUrl != null) {
            if (NULL_PLACEHOLDER.equals(cachedUrl)) {
                throw new BusinessException(ResultCode.SHORTLINK_NOT_FOUND);
            }
            incrementClicks(shortCode);
            return cachedUrl;
        }

        // 二级缓存/回源：MySQL
        ShortLinkDO shortLinkDO = lambdaQuery()
                .eq(ShortLinkDO::getShortCode, shortCode)
                .eq(ShortLinkDO::getStatus, 1)
                .one();

        if (shortLinkDO == null) {
            // 空值缓存防穿透
            cacheNull(shortCode);
            throw new BusinessException(ResultCode.SHORTLINK_NOT_FOUND);
        }

        // 校验过期时间
        if (shortLinkDO.getExpireTime() != null && shortLinkDO.getExpireTime().isBefore(LocalDateTime.now())) {
            cacheNull(shortCode);
            throw new BusinessException(ResultCode.SHORTLINK_EXPIRED);
        }

        cacheUrl(shortCode, shortLinkDO.getOriginalUrl(), shortLinkDO.getExpireTime());
        incrementClicks(shortCode);
        return shortLinkDO.getOriginalUrl();
    }

    @Override
    public ShortLinkRespDTO getByShortCode(String shortCode) {
        ShortLinkDO shortLinkDO = lambdaQuery()
                .eq(ShortLinkDO::getShortCode, shortCode)
                .one();
        if (shortLinkDO == null) {
            throw new BusinessException(ResultCode.SHORTLINK_NOT_FOUND);
        }
        return toRespDTO(shortLinkDO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShortLinkRespDTO update(Long id, ShortLinkUpdateReqDTO requestParam, Long userId) {
        ShortLinkDO shortLinkDO = getById(id);
        if (shortLinkDO == null) {
            throw new BusinessException(ResultCode.SHORTLINK_NOT_FOUND);
        }
        // 仅创建者可修改
        if (!shortLinkDO.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "No permission to update this short link");
        }

        LambdaUpdateWrapper<ShortLinkDO> wrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                .eq(ShortLinkDO::getId, id)
                .set(requestParam.getOriginalUrl() != null, ShortLinkDO::getOriginalUrl,
                        requestParam.getOriginalUrl() != null ? UrlValidator.normalize(requestParam.getOriginalUrl()) : null)
                .set(requestParam.getExpireTime() != null, ShortLinkDO::getExpireTime, requestParam.getExpireTime())
                .set(requestParam.getStatus() != null, ShortLinkDO::getStatus, requestParam.getStatus());
        baseMapper.update(null, wrapper);

        // 更新后清除Redis缓存，下次访问重新加载
        stringRedisTemplate.delete(SHORTLINK_CACHE + shortLinkDO.getShortCode());

        ShortLinkDO updated = getById(id);
        log.info("短链接更新: id={}, shortCode={}", id, updated.getShortCode());
        return toRespDTO(updated);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id, Long userId) {
        ShortLinkDO shortLinkDO = getById(id);
        if (shortLinkDO == null) {
            throw new BusinessException(ResultCode.SHORTLINK_NOT_FOUND);
        }
        if (!shortLinkDO.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "No permission to delete this short link");
        }
        baseMapper.deleteById(id);
        stringRedisTemplate.delete(SHORTLINK_CACHE + shortLinkDO.getShortCode());
        log.info("短链接删除: id={}, shortCode={}", id, shortLinkDO.getShortCode());
    }

    /** DB确认短码是否存在 */
    private boolean checkDbExists(String shortCode) {
        return baseMapper.exists(
                Wrappers.lambdaQuery(ShortLinkDO.class)
                        .eq(ShortLinkDO::getShortCode, shortCode)
        );
    }

    /** 缓存URL到Redis，TTL跟随过期时间或默认24小时 */
    private void cacheUrl(String shortCode, String url, LocalDateTime expireTime) {
        String cacheKey = SHORTLINK_CACHE + shortCode;
        if (expireTime != null) {
            long ttl = java.time.Duration.between(LocalDateTime.now(), expireTime).getSeconds();
            if (ttl > 0) {
                stringRedisTemplate.opsForValue().set(cacheKey, url, ttl, TimeUnit.SECONDS);
                return;
            }
        }
        stringRedisTemplate.opsForValue().set(cacheKey, url, 24, TimeUnit.HOURS);
    }

    /** 缓存空值防止穿透，5分钟过期 */
    private void cacheNull(String shortCode) {
        String cacheKey = SHORTLINK_CACHE + shortCode;
        stringRedisTemplate.opsForValue().set(cacheKey, NULL_PLACEHOLDER, 5, TimeUnit.MINUTES);
    }

    /** Redis点击数递增 */
    private void incrementClicks(String shortCode) {
        String clicksKey = RedisConstants.SHORTLINK_CLICKS + shortCode;
        stringRedisTemplate.opsForValue().increment(clicksKey);
    }

    private ShortLinkRespDTO toRespDTO(ShortLinkDO entity) {
        return ShortLinkRespDTO.builder()
                .id(entity.getId())
                .shortCode(entity.getShortCode())
                .shortUrl(SHORT_DOMAIN + "/" + entity.getShortCode())
                .originalUrl(entity.getOriginalUrl())
                .title(entity.getTitle())
                .totalClicks(entity.getTotalClicks())
                .status(entity.getStatus())
                .expireTime(entity.getExpireTime())
                .createTime(entity.getCreateTime())
                .build();
    }
}
