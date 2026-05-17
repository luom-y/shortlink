package com.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shortlink.admin.dao.entity.UserDO;
import com.shortlink.admin.dao.mapper.UserMapper;
import com.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.shortlink.admin.dto.req.UserUpdateReqDTO;
import com.shortlink.admin.dto.resp.UserRespDTO;
import com.shortlink.admin.service.UserService;
import com.shortlink.common.exception.BusinessException;
import com.shortlink.common.result.Result;
import com.shortlink.common.result.ResultCode;
import com.shortlink.common.result.Results;
import com.shortlink.common.utils.BCryptEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {


    @Override
    public void register(UserRegisterReqDTO requestParam) {
        if (StrUtil.isBlank(requestParam.getUsername())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "用户名不能为空");
        }
        if (StrUtil.isBlank(requestParam.getPassword())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "密码不能为空");
        }
        if(hasUsername(requestParam.getUsername())){
            throw new BusinessException(ResultCode.BAD_REQUEST, "用户名已存在");
        }
        UserDO userDO = new UserDO();
        userDO.setUsername(requestParam.getUsername());
        userDO.setPassword(BCryptEncoder.encode(requestParam.getPassword()));
        userDO.setStatus(1);
        baseMapper.insert(userDO);

    }

    @Override
    public void update(Long id,UserUpdateReqDTO requestParam) {
        if(baseMapper.selectById(id) == null){
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        LambdaUpdateWrapper<UserDO> wrapper = Wrappers.lambdaUpdate(UserDO.class)
                .eq(UserDO::getId, id)
                .set(requestParam.getUsername() != null, UserDO::getUsername, requestParam.getUsername())
                .set(requestParam.getEmail() != null, UserDO::getEmail, requestParam.getEmail())
                .set(requestParam.getPhone() != null, UserDO::getPhone, requestParam.getPhone());
        if (StrUtil.isNotBlank(requestParam.getPassword())) {
            wrapper.set(UserDO::getPassword, BCryptEncoder.encode(requestParam.getPassword()));
        }

        baseMapper.update(null,wrapper);
    }



    public Boolean hasUsername(String username) {
         return  baseMapper.exists(
                 Wrappers.lambdaQuery(UserDO.class)
                            .eq(UserDO::getUsername, username)
         );
     }
}
