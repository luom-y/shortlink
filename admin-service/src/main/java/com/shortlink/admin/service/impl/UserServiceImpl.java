package com.shortlink.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shortlink.admin.dao.entity.UserDO;
import com.shortlink.admin.dao.mapper.UserMapper;
import com.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.shortlink.admin.dto.resp.UserRespDTO;
import com.shortlink.admin.service.UserService;

public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

    @Override
    public Result<UserRespDTO> register(UserRegisterReqDTO requestParam) {
        return null;
    }
}
