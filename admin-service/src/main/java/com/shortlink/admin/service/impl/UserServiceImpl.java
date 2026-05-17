package com.shortlink.admin.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shortlink.admin.dao.entity.UserDO;
import com.shortlink.admin.dao.mapper.UserMapper;
import com.shortlink.admin.dto.req.UserLoginReqDTO;
import com.shortlink.admin.dto.req.UserRefreshTokenReqDTO;
import com.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.shortlink.admin.dto.req.UserUpdateReqDTO;
import com.shortlink.admin.dto.resp.UserLoginRespDTO;
import com.shortlink.admin.service.UserService;
import com.shortlink.admin.utils.JwtUtil;
import com.shortlink.common.exception.BusinessException;
import com.shortlink.common.result.ResultCode;
import com.shortlink.common.utils.BCryptEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

import static com.shortlink.admin.constants.RedisConstants.ACCESS_TOKEN;
import static com.shortlink.admin.constants.RedisConstants.REFRESH_TOKEN;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

    private final JwtUtil jwtUtil;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(UserRegisterReqDTO requestParam) {
        if (StrUtil.isBlank(requestParam.getUsername())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "用户名不能为空");
        }
        if (StrUtil.isBlank(requestParam.getPassword())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "密码不能为空");
        }
        if (hasUsername(requestParam.getUsername())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "用户名已存在");
        }
        UserDO userDO = new UserDO();
        userDO.setUsername(requestParam.getUsername());
        userDO.setPassword(BCryptEncoder.encode(requestParam.getPassword()));
        userDO.setEmail(requestParam.getEmail());
        userDO.setPhone(requestParam.getPhone());
        userDO.setRole("USER");
        userDO.setStatus(1);
        baseMapper.insert(userDO);
    }

    @Override
    public UserLoginRespDTO login(UserLoginReqDTO requestParam) {
        //检查用户是否存在
        UserDO userDO = lambdaQuery()
                .eq(UserDO::getUsername, requestParam.getUsername())
                .one();
        if (userDO == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if (!BCryptEncoder.matches(requestParam.getPassword(), userDO.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }
        if (userDO.getStatus() != null && userDO.getStatus() == 0) {
            throw new BusinessException(ResultCode.FORBIDDEN, "账号已被禁用");
        }
        //生成用户Token
        Long userId = userDO.getId();
        String access = ACCESS_TOKEN + userId;
        String refresh = REFRESH_TOKEN + userId;
        String accessToken = jwtUtil.generateAccessToken(userDO.getId(), userDO.getRole());
        stringRedisTemplate.opsForValue().set(access,accessToken,2, TimeUnit.HOURS);
        String refreshToken = jwtUtil.generateRefreshToken(userDO.getId(), userDO.getRole());
        stringRedisTemplate.opsForValue().set(refresh,refreshToken,7, TimeUnit.DAYS);
        log.info("用户登录成功: userId={}, username={}", userDO.getId(), userDO.getUsername());
        return UserLoginRespDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtUtil.getAccessTokenTtl())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserLoginRespDTO refreshToken(UserRefreshTokenReqDTO requestParam) {
        String oldRefreshToken = requestParam.getRefreshToken();
        Long userId;

        // 解析oldRefreshToken获取用户ID
        try {
            userId = jwtUtil.getUserId(oldRefreshToken);
        } catch (Exception e) {
            log.warn("refreshToken 解析失败", e);
            throw new BusinessException(ResultCode.TOKEN_INVALID, "refreshToken无效或已过期");
        }

        //验证Redis中是否存在oldRefreshToken
        String refreshKey = REFRESH_TOKEN + userId;
        if(!oldRefreshToken.equals(stringRedisTemplate.opsForValue().get(refreshKey))) {
            throw new BusinessException(ResultCode.TOKEN_EXPIRED,"Token已失效");
        }

        //验证用户
        UserDO userDO = getById(userId);
        if (userDO == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if (userDO.getStatus() != null && userDO.getStatus() == 0) {
            throw new BusinessException(ResultCode.FORBIDDEN, "账号已被禁用");
        }
        //生成新的Token
        String accessKey  = ACCESS_TOKEN + userId;
        String newAccessToken = jwtUtil.generateAccessToken(userId, userDO.getRole());
        String newRefreshToken = jwtUtil.generateRefreshToken(userId, userDO.getRole());
        stringRedisTemplate.delete(refreshKey);
        stringRedisTemplate.delete(accessKey);
        stringRedisTemplate.opsForValue().set(accessKey, newAccessToken, 2, TimeUnit.HOURS);
        stringRedisTemplate.opsForValue().set(refreshKey+ userId, newRefreshToken, 7, TimeUnit.DAYS);
        return UserLoginRespDTO.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .expiresIn(jwtUtil.getAccessTokenTtl())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, UserUpdateReqDTO requestParam) {
        UserDO userDO = getById(id);
        if (userDO == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if (StrUtil.isNotBlank(requestParam.getUsername())
                && !requestParam.getUsername().equals(userDO.getUsername())
                && hasUsername(requestParam.getUsername())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "用户名已存在");
        }
        LambdaUpdateWrapper<UserDO> wrapper = Wrappers.lambdaUpdate(UserDO.class)
                .eq(UserDO::getId, id)
                .set(requestParam.getUsername() != null, UserDO::getUsername, requestParam.getUsername())
                .set(requestParam.getEmail() != null, UserDO::getEmail, requestParam.getEmail())
                .set(requestParam.getPhone() != null, UserDO::getPhone, requestParam.getPhone());
        if (StrUtil.isNotBlank(requestParam.getPassword())) {
            wrapper.set(UserDO::getPassword, BCryptEncoder.encode(requestParam.getPassword()));
        }
        baseMapper.update(null, wrapper);
    }

    private Boolean hasUsername(String username) {
        return baseMapper.exists(
                Wrappers.lambdaQuery(UserDO.class)
                        .eq(UserDO::getUsername, username)
        );
    }
}
