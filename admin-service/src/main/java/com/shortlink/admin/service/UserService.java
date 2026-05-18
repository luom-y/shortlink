package com.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shortlink.admin.dao.entity.UserDO;
import com.shortlink.admin.dto.req.UserLoginReqDTO;
import com.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.shortlink.admin.dto.req.UserRefreshTokenReqDTO;
import com.shortlink.admin.dto.req.UserUpdateReqDTO;
import com.shortlink.admin.dto.resp.UserLoginRespDTO;

/**
 * 用户业务接口。失败时抛出 BusinessException。
 */
public interface UserService extends IService<UserDO> {

    void register(UserRegisterReqDTO requestParam);

    UserLoginRespDTO login(UserLoginReqDTO requestParam);

    UserLoginRespDTO refreshToken(UserRefreshTokenReqDTO requestParam);

    void logout(String accessToken);

    void update(Long id, UserUpdateReqDTO requestParam);
}
