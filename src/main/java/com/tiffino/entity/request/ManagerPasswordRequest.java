package com.tiffino.entity.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ManagerPasswordRequest {

    private String managerId ;
    private int otp ;
    private String newPassword ;
}
