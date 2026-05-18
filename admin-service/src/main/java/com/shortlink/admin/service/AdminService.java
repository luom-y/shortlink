package com.shortlink.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shortlink.admin.dao.entity.ShortLinkDO;
import com.shortlink.admin.dao.entity.UserDO;

public interface AdminService {

    IPage<UserDO> listUsers(int page, int size);

    void toggleUserStatus(Long userId, Integer status);

    IPage<ShortLinkDO> listShortLinks(int page, int size, String shortCode);

    void toggleShortLinkStatus(Long id, Integer status);

    void forceDeleteShortLink(Long id);
}
