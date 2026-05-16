package com.shortlink.admin.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserRefreshTokenReqDTO {

    @NotBlank(message = "refreshToken不能为空")
    private String refreshToken;
}
