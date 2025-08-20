package com.tiffino.entity.response;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdationResponse {
    // Map of only the fields that actually changed → new values
    private Map<String, Object> updated;
    private List<String> changedFields;

}
