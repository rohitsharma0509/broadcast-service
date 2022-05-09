package com.scb.rider.broadcast.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class JobServiceException extends RuntimeException {
    private String errorMessage;
}
