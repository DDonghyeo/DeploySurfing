package com.ds.deploysurfingbackend.domain.app.entity.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AppStatus {

    INITIAL("초기"),
    EC2_CREATION_STARTED("EC2 생성 시작됨"),
    EC2_CREATION_COMPLETED("EC2 생성 완료"),
    EC2_LAUNCH_STARTED("EC2 실행 시작"),
    EC2_LAUNCH_COMPLETED("EC2 실행 완료"),
    ELASTIC_IP_ALLOCATION_IN_PROGRESS("탄력적 IP 할당중"),
    ELASTIC_IP_ALLOCATION_COMPLETED("탄력적 IP 할당 완료"),
    ACTION_SECRET_CONFIGURATION_IN_PROGRESS("ACTION SECRET 설정 중"),
    DEPLOY_BRANCH_CREATION_IN_PROGRESS("DEPLOY BRANCH 생성 중"),
    DOCKERFILE_CREATION_IN_PROGRESS("DOCKERFILE 생성 중"),
    DEPLOY_YML_CREATION_IN_PROGRESS("배포 스크립트 생성 중"),
    CONFIGURATION_COMPLETED("설정 완료"),
    RUNNING("실행 중"),
    PAUSED("중지됨"),
    TERMINATED("종료됨"),
    DELETED("삭제됨")
    ;

    private final String status;


    public String getStatus() {
        return status;
    }

}