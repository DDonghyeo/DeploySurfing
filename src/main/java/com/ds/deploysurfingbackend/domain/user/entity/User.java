package com.ds.deploysurfingbackend.domain.user.entity;

import com.ds.deploysurfingbackend.domain.app.entity.BaseTimeEntity;
import com.ds.deploysurfingbackend.domain.user.dto.request.UserRequest;
import com.ds.deploysurfingbackend.domain.user.entity.type.UserStatus;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Entity
@Table(name = "user")
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String email;

    private String password;

    private UserStatus status;

    private String awsRoleArn;

    private String awsAccessKey;

    private String awsSecretKey;

    private String dockerToken;

    private String gitHubToken;

    public void update(UserRequest.UpdateDto updateDto) {
        name = updateDto.name() == null ? name : updateDto.name();
        awsRoleArn = updateDto.awsRoleArn() == null ? awsRoleArn : updateDto.awsRoleArn();
        awsAccessKey = updateDto.awsAccessKey() == null ? awsAccessKey : updateDto.awsAccessKey();
        awsSecretKey = updateDto.awsSecretKey() == null ? awsSecretKey : updateDto.awsSecretKey();
        dockerToken = updateDto.dockerToken() == null ? dockerToken : updateDto.dockerToken();
        gitHubToken = updateDto.gitHubToken() == null ? gitHubToken : updateDto.gitHubToken();
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }
}
