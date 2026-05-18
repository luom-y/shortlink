package com.shortlink.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shortlink.admin.dao.entity.ShortLinkDO;
import com.shortlink.admin.dao.entity.UserDO;
import com.shortlink.admin.dao.mapper.ShortLinkMapper;
import com.shortlink.admin.dao.mapper.UserMapper;
import com.shortlink.admin.service.AdminService;
import com.shortlink.common.exception.BusinessException;
import com.shortlink.common.result.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 后台管理服务实现：用户管理、短链接管理。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserMapper userMapper;
    private final ShortLinkMapper shortLinkMapper;

    @Override
    public IPage<UserDO> listUsers(int page, int size) {
        return userMapper.selectPage(
                new Page<>(page, size),
                Wrappers.lambdaQuery(UserDO.class)
                        .orderByDesc(UserDO::getCreateTime)
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleUserStatus(Long userId, Integer status) {
        UserDO userDO = userMapper.selectById(userId);
        if (userDO == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        userMapper.update(null,
                Wrappers.lambdaUpdate(UserDO.class)
                        .eq(UserDO::getId, userId)
                        .set(UserDO::getStatus, status)
        );
        log.info("用户状态切换: userId={}, newStatus={}", userId, status);
    }

    @Override
    public IPage<ShortLinkDO> listShortLinks(int page, int size, String shortCode) {
        LambdaQueryWrapper<ShortLinkDO> wrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                .orderByDesc(ShortLinkDO::getCreateTime);
        if (shortCode != null && !shortCode.isBlank()) {
            wrapper.eq(ShortLinkDO::getShortCode, shortCode);
        }
        return shortLinkMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleShortLinkStatus(Long id, Integer status) {
        ShortLinkDO shortLinkDO = shortLinkMapper.selectById(id);
        if (shortLinkDO == null) {
            throw new BusinessException(ResultCode.SHORTLINK_NOT_FOUND);
        }
        shortLinkMapper.update(null,
                Wrappers.lambdaUpdate(ShortLinkDO.class)
                        .eq(ShortLinkDO::getId, id)
                        .set(ShortLinkDO::getStatus, status)
        );
        log.info("短链接状态切换: id={}, newStatus={}", id, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void forceDeleteShortLink(Long id) {
        ShortLinkDO shortLinkDO = shortLinkMapper.selectById(id);
        if (shortLinkDO == null) {
            throw new BusinessException(ResultCode.SHORTLINK_NOT_FOUND);
        }
        shortLinkMapper.deleteById(id);
        log.info("管理员强制删除短链接: id={}", id);
    }
}
