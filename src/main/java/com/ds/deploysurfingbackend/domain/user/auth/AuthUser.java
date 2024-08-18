package com.ds.deploysurfingbackend.domain.user.auth;

import com.ds.deploysurfingbackend.domain.user.entity.type.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class AuthUser {

        private final Long id;
        private final String email;
        @JsonIgnore
        private final String password;
        private final List<Role> roles;
}
