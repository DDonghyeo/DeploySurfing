package com.ds.deploysurfingbackend.domain.user.exception;

import com.ds.deploysurfingbackend.global.exception.ErrorCode;
import com.ds.deploysurfingbackend.global.response.ApiResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@Getter
@AllArgsConstructor
public enum UserErrorCode implements ErrorCode {

    USER_NOT_FOUND(NOT_FOUND, "USER_404", "해당 유저 정보를 찾을 수 없습니다"),
    USER_ALREADY_REGISTERED(CONFLICT, "USER_409", "이미 존재하는 사용자입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ApiResponse<Void> getErrorResponse() {
        return ApiResponse.onFailure(code, message);
    }
}
