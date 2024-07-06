package com.ds.deploysurfingbackend.domain.user.service;

import com.ds.deploysurfingbackend.domain.user.dto.request.UserRequest;
import com.ds.deploysurfingbackend.domain.user.dto.resoonse.UserResponseDto;
import com.ds.deploysurfingbackend.domain.user.entity.User;
import com.ds.deploysurfingbackend.domain.user.entity.type.UserStatus;
import com.ds.deploysurfingbackend.domain.user.repository.UserRepository;
import com.ds.deploysurfingbackend.global.exception.CustomException;
import com.ds.deploysurfingbackend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;

    public void signup(UserRequest.SignUpDto signUpDto) {

        userRepository.save(User.builder()
                        .name(signUpDto.name())
                        .email(signUpDto.email())
                        //TODO : 패스워드 암호화
                        .password(signUpDto.password())
                .status(UserStatus.ACTIVE)
                .build()
        );
    }
    public UserResponseDto getUser(String email) {
        return UserResponseDto.from(
                userRepository.findByEmail(email).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND))
        );
    }

    public void updateUser(String email, UserRequest.UpdateDto updateDto) {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND));
        user.update(updateDto);
    }

    public void deleteUser(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND));
        //soft delete
        user.setStatus(UserStatus.DELETED);
    }
}
