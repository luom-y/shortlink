package com.shortlink.shortlink.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shortlink.shortlink.dao.entity.ShortLinkDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ShortLinkMapper extends BaseMapper<ShortLinkDO> {
}
