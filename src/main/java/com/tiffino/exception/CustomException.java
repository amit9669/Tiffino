package com.tiffino.exception;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
public class CustomException extends RuntimeException {

    private String message;
}
