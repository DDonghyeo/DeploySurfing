package com.ds.deploysurfingbackend.global.event.events;

import com.ds.deploysurfingbackend.global.event.DeploymentEvent;
import com.ds.deploysurfingbackend.global.event.EventType;
import lombok.Builder;
import lombok.Getter;

public class GithubEvents {

    public class SecretConfigurationEvent extends DeploymentEvent {

        public SecretConfigurationEvent(SecretConfigPayload payload) {
            super(EventType.CONFIG_SECRET, payload.toString());
        }

        @Getter
        @Builder
        public class SecretConfigPayload {
            private final String userEmail;
            private final String githubToken;
            private final String ec2InstanceId;
            private final String ec2PublicIp;
        }
    }

    // Deploy 브랜치 생성 이벤트
    public class DeployBranchCreationEvent extends DeploymentEvent {
        public DeployBranchCreationEvent(String payload) {
            super(EventType.CREATE_BRANCH, payload);
        }
    }

    public class CreateFileEvent extends DeploymentEvent {

        public CreateFileEvent(EventType eventType, String payload) {
            super(eventType, payload);
        }

        @Getter
        @Builder
        public class GitHubPayload {
            private final String githubToken;
            private final String owner;
            private final String repoName;
        }
    }



}
