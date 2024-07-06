package com.ds.deploysurfingbackend.domain.user.dto.request;

import lombok.Builder;

public class UserRequest {

    @Builder
    public record UpdateDto(
           String name,
           String awsRoleArn,
           String awsAccessKey,
           String awsSecretKey,
           String dockerToken,
           String gitHubToken
    ){}

    @Builder
    public record SignUpDto(
            String name,
            String email,
            String password
    ){}
}
