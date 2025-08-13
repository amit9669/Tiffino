package com.tiffino.entity.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LogInResponse {

    private String jwtToken;
}
