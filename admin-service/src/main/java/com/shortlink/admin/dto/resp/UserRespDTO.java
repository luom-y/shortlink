package com.shortlink.admin.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRespDTO {

    private Long id;

    private String username;

    private String email;

    private String phone;

    private String role;


}
