package com.ds.deploysurfingbackend.global.event.events;

import com.ds.deploysurfingbackend.global.event.DeploymentEvent;
import com.ds.deploysurfingbackend.global.event.EventType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

public class EC2Events {

    // EC2 생성 이벤트
    public class EC2CreationEvent extends DeploymentEvent {

        public EC2CreationEvent(String payload) {
            super(EventType.CREATE_EC2, payload);
        }


        @Getter
        @Builder
        public static class EC2CreationPayload {
            private final String awsAccessKey;
            private final String awsSecretKey;
            private final String appName;
        }
    }

}
