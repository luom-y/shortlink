package com.shortlink.admin.dto.req;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * User update request — all fields optional for partial update.
 */
@Data
public class UserUpdateReqDTO {

    @Size(min = 3, max = 64, message = "Username must be 3-64 characters")
    private String username;

    @Size(min = 6, max = 32, message = "Password must be 6-32 characters")
    private String password;

    @Email(message = "Invalid email format")
    private String email;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "Invalid phone number")
    private String phone;
}
