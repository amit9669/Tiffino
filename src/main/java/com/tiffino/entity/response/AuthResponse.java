package com.tiffino.entity.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthResponse {

    private String jwtToken;
    private String role;
    private String message;


    public AuthResponse(String message){
        this.message = message;
    }
}
