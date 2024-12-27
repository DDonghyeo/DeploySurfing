package com.ds.deploysurfingbackend.global.event;

public enum EventType {
    CREATE_EC2, //1. EC2 생성
    CONFIG_SECRET, //2. Action Secret 구성
    CREATE_BRANCH, //3. deploy branch 생성
    CREATE_FILE, //4. dockerfile 생성
    CREATE_SCRIPT, //5. 스크립트 생성

}
