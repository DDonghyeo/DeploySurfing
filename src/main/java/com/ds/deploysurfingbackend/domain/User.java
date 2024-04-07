package com.ds.deploysurfingbackend.domain;

import com.ds.deploysurfingbackend.domain.type.AppStatus;
import com.ds.deploysurfingbackend.domain.type.AppType;
import com.ds.deploysurfingbackend.dto.AppDto;
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
