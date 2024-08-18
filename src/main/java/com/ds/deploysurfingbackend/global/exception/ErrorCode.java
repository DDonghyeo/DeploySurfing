package com.ds.deploysurfingbackend.global.exception;

import com.ds.deploysurfingbackend.global.response.ApiResponse;
import org.springframework.http.HttpStatus;

public interface ErrorCode {

    HttpStatus getHttpStatus();

    String getCode();

    String getMessage();

    ApiResponse<Void> getErrorResponse();
}

