package com.ds.deploysurfingbackend.domain.github.exception;

import com.ds.deploysurfingbackend.global.exception.ErrorCode;
import com.ds.deploysurfingbackend.global.response.ApiResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Getter
@AllArgsConstructor
public enum GithubErrorCode implements ErrorCode {

    // App 관련 에러
    GITHUB_METADATA_NOT_FOUND(NOT_FOUND, "GITHUB_404", "깃허브 데이터를 찾을 수 없습니다."),
    APP_ALREADY_INITIALIZED(INTERNAL_SERVER_ERROR, "GITHUB_501", "이미 초기화 된 앱입니다."),

    DEPLOY_BRANCH_ALREADY_EXISTS(INTERNAL_SERVER_ERROR, "GITHUB_502", "배포 브랜치가 이미 존재합니다."),
    INFORMAL_GITHUB_URL(INTERNAL_SERVER_ERROR, "GITHUB_503", "올바르지 않은 Github Repository URL 입니다."),
    INVALID_GITHUB_TOKEN(INTERNAL_SERVER_ERROR, "GITHUB_403", "올바르지 않은 깃허브 토큰입니다.")

    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ApiResponse<Void> getErrorResponse() {
        return ApiResponse.onFailure(code, message);
    }
}
