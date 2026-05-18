package com.shortlink.shortlink.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ShortLinkCreateReqDTO {

    @NotBlank(message = "Original URL cannot be empty")
    private String originalUrl;

    /** Optional custom expiry time */
    private LocalDateTime expireTime;
}
