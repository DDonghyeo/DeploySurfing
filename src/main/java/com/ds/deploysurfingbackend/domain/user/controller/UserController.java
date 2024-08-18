package com.ds.deploysurfingbackend.domain.user.controller;

import com.ds.deploysurfingbackend.domain.user.auth.AuthUser;
import com.ds.deploysurfingbackend.domain.user.dto.request.JwtDto;
import com.ds.deploysurfingbackend.domain.user.dto.request.LoginRequestDto;
import com.ds.deploysurfingbackend.domain.user.dto.request.UserRequest;
import com.ds.deploysurfingbackend.domain.user.service.UserService;
import com.ds.deploysurfingbackend.global.annotation.CurrentUser;
import com.ds.deploysurfingbackend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/user")
@RestController
@Tag(name = "User Controller")
public class UserController {

    private final UserService userService;

    @Operation(description = "사용자 정보 조회")
    @GetMapping("")
    public ApiResponse<?> getUser(@CurrentUser AuthUser authUser) {
        return ApiResponse.onSuccess(userService.getUser(authUser));
    }

    @Operation(description = "사용자 회원가입")
    @PostMapping("/signup")
    public ApiResponse<?> createUser(UserRequest.SignUpDto signUpDto) {
        userService.signup(signUpDto);
        return ApiResponse.onSuccess(HttpStatus.CREATED, "회원가입 완료");
    }

    @Operation(description = "사용자 정보 수정")
    @PostMapping("/update")
    public ApiResponse<?> updateUser(@CurrentUser AuthUser authUser,
                                     UserRequest.UpdateDto updateDto) {
        userService.updateUser(authUser, updateDto);
        return ApiResponse.onSuccess(HttpStatus.OK, "수정이 완료 되었습니다.");
    }

    @Operation(description = "사용자 탈퇴")
    @DeleteMapping("/withdraw")
    public ApiResponse<?> deleteUser(@CurrentUser AuthUser authUser) {
        userService.deleteUser(authUser);
        return ApiResponse.onSuccess(HttpStatus.OK, "삭제가 완료 되었습니다.");
    }

    //Swagger용 가짜 컨트롤러
    @Operation(description = "로그인")
    @PostMapping("/login")
    public ApiResponse<JwtDto> login(@RequestBody LoginRequestDto loginRequestDto) {
        return null;
    }
}
