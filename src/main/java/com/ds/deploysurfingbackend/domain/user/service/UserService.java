package com.ds.deploysurfingbackend.domain.user.service;

import com.ds.deploysurfingbackend.domain.user.auth.AuthUser;
import com.ds.deploysurfingbackend.domain.user.dto.request.UserRequest;
import com.ds.deploysurfingbackend.domain.user.dto.resoonse.UserResponseDto;
import com.ds.deploysurfingbackend.domain.user.entity.User;
import com.ds.deploysurfingbackend.domain.user.entity.type.Role;
import com.ds.deploysurfingbackend.domain.user.entity.type.UserStatus;
import com.ds.deploysurfingbackend.domain.user.repository.UserRepository;
import com.ds.deploysurfingbackend.global.exception.CustomException;
import com.ds.deploysurfingbackend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void signup(UserRequest.SignUpDto signUpDto) {

        String email = signUpDto.email();
        if (userRepository.existsByEmail(email)) {
            throw new CustomException(ErrorCode.USER_ALREADY_REGISTERED);
        }
        userRepository.save(signUpDto.toEntity(passwordEncoder));
    }

    @Transactional(readOnly = true)
    public UserResponseDto getUser(AuthUser authUser) {
        return UserResponseDto.from(
                userRepository.findByEmail(authUser.getEmail()).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND))
        );
    }

    @Transactional
    public void updateUser(AuthUser authUser, UserRequest.UpdateDto updateDto) {
        User user = userRepository.findByEmail(authUser.getEmail()).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND));
        user.update(updateDto);
    }

    @Transactional
    public void deleteUser(AuthUser authUser) {
        User user = userRepository.findByEmail(authUser.getEmail()).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND));
        //soft delete
        user.setStatus(UserStatus.DELETED);
    }
}
