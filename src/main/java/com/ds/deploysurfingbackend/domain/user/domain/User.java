package com.ds.deploysurfingbackend.domain.user.domain;

import com.ds.deploysurfingbackend.domain.app.domain.BaseTimeEntity;
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

    private String dockerToken;
    private String awsToken;
    private String gitHubToken;
}
