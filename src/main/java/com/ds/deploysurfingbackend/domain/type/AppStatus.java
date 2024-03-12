package com.ds.deploysurfingbackend.domain.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AppStatus {

    TERMINATED(0), // 종료됨
    RUNNING(1), // 실행 중
    PAUSED(2); // 중지

    private final Integer status;

    public Integer getStatus() {
        return this.status;
    }
}
