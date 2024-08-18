package com.ds.deploysurfingbackend.global.filter;

import com.ds.deploysurfingbackend.domain.user.auth.CustomUserDetails;
import com.ds.deploysurfingbackend.domain.user.auth.UserRepositoryUserDetailsService;
import com.ds.deploysurfingbackend.domain.user.entity.User;
import com.ds.deploysurfingbackend.domain.user.entity.type.Role;
import com.ds.deploysurfingbackend.domain.user.repository.UserRepository;
import com.ds.deploysurfingbackend.global.utils.HttpResponseUtil;
import com.ds.deploysurfingbackend.global.utils.JwtUtil;
import com.ds.deploysurfingbackend.global.utils.RedisUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            String accessToken = jwtUtil.resolveAccessToken(request);

            // accessToken 없이 접근 -> 필터를 건너뜀
            if (accessToken == null) {
                filterChain.doFilter(request, response);
                return;
            }

             //logout 처리된 accessToken
            if (redisUtil.get(accessToken) != null && redisUtil.get(accessToken).equals("logout")) {
                log.warn("[ JwtAuthorizationFilter ] 로그아웃된 엑세스 토큰입니다.");
                HttpResponseUtil.setErrorResponse(response, HttpStatus.UNAUTHORIZED, "로그아웃 처리된 토큰입니다." );
                filterChain.doFilter(request, response);
                return;
            }

            authenticateAccessToken(accessToken);
            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException e) {
            log.warn("[ JwtAuthorizationFilter ] 만료된 엑세스 토큰입니다.");
            HttpResponseUtil.setErrorResponse(response, HttpStatus.UNAUTHORIZED, "만료된 토큰입니다." );
        }
    }

    private void authenticateAccessToken(String accessToken) {

        //AccessToken 유효성 검증
        jwtUtil.validateToken(accessToken);

        //CustomUserDetail 객체 생성
        CustomUserDetails userDetails = new CustomUserDetails(
                jwtUtil.getId(accessToken),
                jwtUtil.getEmail(accessToken),
                null,
                jwtUtil.getRoles(accessToken)
        );

        // Spring Security 인증 토큰 생성
        Authentication authToken = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities());

        // SecurityContextHolder 인증 객체 저장
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }
}
