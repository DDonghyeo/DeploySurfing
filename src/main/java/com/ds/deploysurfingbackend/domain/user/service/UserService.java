package com.ds.deploysurfingbackend.domain.user.service;

import com.ds.deploysurfingbackend.domain.user.dto.request.UserRequest;
import com.ds.deploysurfingbackend.domain.user.dto.resoonse.UserResponseDto;
import com.ds.deploysurfingbackend.domain.user.entity.User;
import com.ds.deploysurfingbackend.domain.user.entity.type.UserStatus;
import com.ds.deploysurfingbackend.domain.user.repository.UserRepository;
import com.ds.deploysurfingbackend.global.exception.CustomException;
import com.ds.deploysurfingbackend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public UserResponseDto getUser(String email) {
        return UserResponseDto.from(
                userRepository.findByEmail(email).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND))
        );
    }

    @Transactional
    public void updateUser(String email, UserRequest.UpdateDto updateDto) {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND));
        user.update(updateDto);
    }

    @Transactional
    public void deleteUser(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND));
        //soft delete
        user.setStatus(UserStatus.DELETED);
    }
}
