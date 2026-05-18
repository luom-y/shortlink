package com.shortlink.shortlink.dao.entity;

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

    /** Short code, e.g. "aBc123" */
    private String shortCode;

    /** Original long URL */
    private String originalUrl;

    /** Title extracted from the original page */
    private String title;

    /** Creator user ID */
    private Long userId;

    /** Expiration time, null means no expiration */
    private LocalDateTime expireTime;

    /** Total clicks (denormalized from statistics) */
    private Long totalClicks;

    /** Status: 1=active, 0=disabled */
    private Integer status;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
