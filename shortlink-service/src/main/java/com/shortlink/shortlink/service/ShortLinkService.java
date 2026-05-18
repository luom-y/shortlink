package com.shortlink.shortlink.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shortlink.shortlink.dao.entity.ShortLinkDO;
import com.shortlink.shortlink.dto.req.ShortLinkCreateReqDTO;
import com.shortlink.shortlink.dto.req.ShortLinkUpdateReqDTO;
import com.shortlink.shortlink.dto.resp.ShortLinkRespDTO;

public interface ShortLinkService extends IService<ShortLinkDO> {

    ShortLinkRespDTO create(ShortLinkCreateReqDTO requestParam, Long userId);

    String redirect(String shortCode);

    ShortLinkRespDTO getByShortCode(String shortCode);

    ShortLinkRespDTO update(Long id, ShortLinkUpdateReqDTO requestParam, Long userId);

    void delete(Long id, Long userId);
}
