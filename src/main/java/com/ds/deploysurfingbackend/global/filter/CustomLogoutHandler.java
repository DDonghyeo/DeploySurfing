package com.ds.deploysurfingbackend.global.filter;

import com.ds.deploysurfingbackend.global.exception.CustomException;
import com.ds.deploysurfingbackend.global.exception.CommonErrorCode;
import com.ds.deploysurfingbackend.global.utils.JwtUtil;
import com.ds.deploysurfingbackend.global.utils.RedisUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Slf4j
public class CustomLogoutHandler implements LogoutHandler {

    private final RedisUtil redisUtil;
    private final JwtUtil jwtUtil;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        try {

            String accessToken = jwtUtil.resolveAccessToken(request);

            redisUtil.save(
                    accessToken,
                    "logout",
                    jwtUtil.getExpTime(accessToken),
                    TimeUnit.MILLISECONDS
            );

            String email = jwtUtil.getEmail(accessToken);

            redisUtil.delete(
                    email + "_refresh_token"
            );

            redisUtil.delete(
                    email + "_fcm_token"
            );

        } catch (ExpiredJwtException e) {
            log.warn("Acess Token이 만료되었습니다.");
            throw new CustomException(CommonErrorCode.TOKEN_EXPIRED);
        }
    }
}
