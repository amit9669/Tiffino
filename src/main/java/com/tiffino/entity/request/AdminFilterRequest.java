package com.tiffino.entity.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminFilterRequest {

    private List<String> state;
    private List<String> city;
    private List<String> division;
}
