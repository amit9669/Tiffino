package com.tiffino.entity.request;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class LogInRequest {

    private String emailOrId;
    private String password;
}
