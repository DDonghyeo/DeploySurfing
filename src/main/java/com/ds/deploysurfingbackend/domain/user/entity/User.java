package com.ds.deploysurfingbackend.domain.user.entity;

import com.ds.deploysurfingbackend.domain.app.entity.BaseTimeEntity;
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

    private String awsRoleArn;

    private String awsAccessKey;

    private String awsSecretKey;

    private String dockerToken;

    private String gitHubToken;
}
