package com.shortlink.admin.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user")
public class UserDO {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String username;

    private String password;       // BCrypt 密文

    private String email;

    private String phone;

    private String role;           // "USER" | "ADMIN"

    private Integer status;        // 1=正常 0=禁用

    @TableLogic
    private Integer deleted;       // 0=存在 1=已删

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
