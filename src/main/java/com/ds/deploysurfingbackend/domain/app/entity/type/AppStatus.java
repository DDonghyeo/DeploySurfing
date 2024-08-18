package com.ds.deploysurfingbackend.domain.app.entity.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AppStatus {

    STARTING, //시작 중
    TERMINATED, // 종료됨
    RUNNING, // 실행 중
    PAUSED; // 중지

}