package com.ds.deploysurfingbackend.global.filter;

import com.ds.deploysurfingbackend.domain.user.auth.CustomUserDetails;
import com.ds.deploysurfingbackend.domain.user.dto.request.JwtDto;
import com.ds.deploysurfingbackend.domain.user.dto.request.LoginRequestDto;
import com.ds.deploysurfingbackend.global.utils.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    //로그인 시도 메서드
    @Override
    public Authentication attemptAuthentication(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response) throws AuthenticationException {

        ObjectMapper objectMapper = new ObjectMapper();
        LoginRequestDto requestBody;
        try {
            requestBody = objectMapper.readValue(request.getInputStream(), LoginRequestDto.class);
        } catch (IOException e) {
            throw new AuthenticationServiceException("[ Login Filter ] Request Body 파싱 과정에서 오류가 발생했습니다.");
        }

        String email = requestBody.email();
        String password = requestBody.password(); //password 추출

        //UserNamePasswordToken 생성 (인증용 객체)
        UsernamePasswordAuthenticationToken authToken
                = new UsernamePasswordAuthenticationToken(email, password, null);

        return authenticationManager.authenticate(authToken);
    }

    //로그인 성공시
    @Override
    protected void successfulAuthentication(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain chain,
            @NonNull Authentication authentication) throws IOException {


        CustomUserDetails customUserDetails = (CustomUserDetails)authentication.getPrincipal();


        //Client 에게 줄 Response
        JwtDto jwtDto = JwtDto.builder()
                .accessToken(jwtUtil.createJwtAccessToken(customUserDetails)) //access token 생성
                .refreshToken(jwtUtil.createJwtRefreshToken(customUserDetails)) //refresh token 생성
                .build();

        ObjectMapper objectMapper = new ObjectMapper();
        response.setStatus(HttpStatus.OK.value()); //Response 의 Status 를 200으로 설정
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        //Body 에 토큰이 담긴 Response 쓰기
        response.getWriter().write(objectMapper.writeValueAsString(jwtDto));
    }

    //로그인 실패시
    @Override
    protected void unsuccessfulAuthentication(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull AuthenticationException failed) throws IOException {
        
        String errorMessage;
        if (failed instanceof BadCredentialsException) {
            errorMessage = "잘못된 정보입니다.";
        } else if (failed instanceof LockedException) {
            errorMessage = "계정이 잠금 상태입니다.";
        } else if (failed instanceof DisabledException) {
            errorMessage = "계정이 비활성화 되었습니다.";
        } else if (failed instanceof UsernameNotFoundException) {
            errorMessage = "계정을 찾을 수 없습니다.";
        } else if (failed instanceof AuthenticationServiceException) {
            errorMessage = "Request Body 파싱 중 오류가 발생했습니다.";
        } else {
            errorMessage = "인증에 실패했습니다.";
        }

        ObjectMapper objectMapper = new ObjectMapper();
        response.setStatus(HttpStatus.UNAUTHORIZED.value()); //Status 설정
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(errorMessage)); //error message 와 함께 Response 작성
    }

}
