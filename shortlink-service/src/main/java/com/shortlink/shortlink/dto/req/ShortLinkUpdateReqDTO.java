package com.shortlink.shortlink.dto.req;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ShortLinkUpdateReqDTO {

    /** New original URL */
    private String originalUrl;

    /** New expiration time */
    private LocalDateTime expireTime;

    /** Status: 1=active, 0=disabled */
    private Integer status;
}
