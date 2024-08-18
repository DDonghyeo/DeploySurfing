package com.ds.deploysurfingbackend.domain.app.exception;

import com.ds.deploysurfingbackend.global.exception.ErrorCode;
import com.ds.deploysurfingbackend.global.response.ApiResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@Getter
@AllArgsConstructor
public enum AppErrorCode implements ErrorCode {

    // App 관련 에러
    APP_ALREADY_INITIALIZED(INTERNAL_SERVER_ERROR, "APP_501", "이미 초기화 된 앱입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ApiResponse<Void> getErrorResponse() {
        return ApiResponse.onFailure(code, message);
    }
}
