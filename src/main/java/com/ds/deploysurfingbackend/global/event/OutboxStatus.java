package com.ds.deploysurfingbackend.global.event;

public enum OutboxStatus {
    CREATED, //생성됨
    PUBLISHED, //발행됨
    FAILED //발행 실패
}
