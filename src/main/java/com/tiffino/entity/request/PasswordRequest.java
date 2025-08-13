package com.tiffino.entity.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PasswordRequest {

    private int otp ;
    private String newPassword ;
    private String confirmNewPassword ;
}
