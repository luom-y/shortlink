package com.shortlink.shortlink.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("click_record")
public class ClickRecordDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String shortCode;

    private String ip;

    private String userAgent;

    private String referer;

    private LocalDateTime createTime;
}
