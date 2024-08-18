package com.ds.deploysurfingbackend.domain.aws.exception;

import com.ds.deploysurfingbackend.global.exception.ErrorCode;
import com.ds.deploysurfingbackend.global.response.ApiResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Getter
@AllArgsConstructor
public enum AwsErrorCode implements ErrorCode {

    EC2_NOT_FOUND(NOT_FOUND, "AWS_404", "해당 EC2를 찾을 수 없습니다"),
    VPC_NOT_FOUND(INTERNAL_SERVER_ERROR, "AWS_500", "VPC가 존재하지 않습니다."),
    DUPLICATE_KEY_NAME(INTERNAL_SERVER_ERROR, "AWS_500", "이미 존재하는 키 페어 이름입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ApiResponse<Void> getErrorResponse() {
        return ApiResponse.onFailure(code, message);
    }
}
