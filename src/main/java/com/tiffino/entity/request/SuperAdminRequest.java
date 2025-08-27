package com.tiffino.entity.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class SuperAdminRequest {
    private String adminName;
    private String email;
    private String password;
}
