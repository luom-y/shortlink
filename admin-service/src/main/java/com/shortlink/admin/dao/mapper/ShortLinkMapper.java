package com.shortlink.admin.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shortlink.admin.dao.entity.ShortLinkDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ShortLinkMapper extends BaseMapper<ShortLinkDO> {
}
