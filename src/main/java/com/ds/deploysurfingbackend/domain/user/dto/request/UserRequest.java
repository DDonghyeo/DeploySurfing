package com.ds.deploysurfingbackend.domain.user.dto.request;

import com.ds.deploysurfingbackend.domain.user.entity.User;
import com.ds.deploysurfingbackend.domain.user.entity.type.Role;
import com.ds.deploysurfingbackend.domain.user.entity.type.UserStatus;
import lombok.Builder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;

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
    ){
        public User toEntity(PasswordEncoder passwordEncoder) {
            return User.builder()
                    .name(name)
                    .email(email)
                    .password(passwordEncoder.encode(password))
                    .status(UserStatus.ACTIVE)
                    .roles(Collections.singletonList(Role.ROLE_USER))
                    .build();
        }
    }
}
