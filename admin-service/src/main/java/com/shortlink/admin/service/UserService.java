package com.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shortlink.admin.dao.entity.UserDO;
import com.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.shortlink.admin.dto.req.UserUpdateReqDTO;
import com.shortlink.admin.dto.resp.UserRespDTO;
import com.shortlink.common.result.Result;

public interface UserService extends IService<UserDO> {


    void register(UserRegisterReqDTO requestParam);

    void update(Long id ,UserUpdateReqDTO requestParam);
}
