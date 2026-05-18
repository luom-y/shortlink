package com.shortlink.admin.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("short_link")
public class ShortLinkDO {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String shortCode;

    private String originalUrl;

    private String title;

    private Long userId;

    private LocalDateTime expireTime;

    private Long totalClicks;

    private Integer status;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
