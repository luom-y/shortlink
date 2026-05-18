package com.shortlink.shortlink.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortLinkRespDTO {

    private Long id;

    private String shortCode;

    private String shortUrl;

    private String originalUrl;

    private String title;

    private Long totalClicks;

    private Integer status;

    private LocalDateTime expireTime;

    private LocalDateTime createTime;
}
