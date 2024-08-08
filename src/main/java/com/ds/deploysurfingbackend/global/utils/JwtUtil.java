package com.ds.deploysurfingbackend.global.utils;

import com.ds.deploysurfingbackend.domain.user.auth.CustomUserDetails;
import com.ds.deploysurfingbackend.domain.user.dto.request.JwtDto;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final Long accessExpMs;
    private final Long refreshExpMs;
    private final RedisUtil redisUtil;

    public JwtUtil(
            @Value("${spring.jwt.secret}") String secret,
            @Value("${spring.jwt.token.access-expiration-time}") Long access,
            @Value("${spring.jwt.token.refresh-expiration-time}") Long refresh,
            RedisUtil redisUtil
    ) {

        secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),
                Jwts.SIG.HS256.key().build().getAlgorithm());
        accessExpMs = access;
        refreshExpMs = refresh;
        this.redisUtil = redisUtil;
    }

    // JWT 토큰을 입력으로 받아 토큰의 subject 로부터 사용자 Email 추출하는 메서드
    public String getEmail(String token) throws SignatureException {
//        log.info("[ JwtUtil ] 토큰에서 이메일을 추출합니다.");
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    // JWT 토큰을 입력으로 받아 토큰의 claim 에서 사용자 권한을 추출하는 메서드
    public String getRoles(String token) throws SignatureException{
//        log.info("[ JwtUtil ] 토큰에서 권한을 추출합니다.");
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);
    }

    // Token 발급하는 메서드
    public String tokenProvider(CustomUserDetails customUserDetails, Instant expiration) {

//        log.info("[ JwtUtil ] 토큰을 새로 생성합니다.");
        //현재 시간
        Instant issuedAt = Instant.now();

        //토큰에 부여할 권한
        String authorities = customUserDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .header() //헤더 부분
                .add("typ", "JWT") // JWT type
                .and()
                .subject(customUserDetails.getUsername()) //Subject 에 username (email) 추가
                .claim("role", authorities) //권한 추가
                .issuedAt(Date.from(issuedAt)) // 현재 시간 추가
                .expiration(Date.from(expiration)) //만료 시간 추가
                .signWith(secretKey) //signature 추가
                .compact(); //합치기
    }

    // principalDetails 객체에 대해 새로운 JWT 액세스 토큰을 생성
    public String createJwtAccessToken(CustomUserDetails customUserDetails) {
        Instant expiration = Instant.now().plusMillis(accessExpMs);
        return tokenProvider(customUserDetails, expiration);
    }

    // principalDetails 객체에 대해 새로운 JWT 리프레시 토큰을 생성
    public String createJwtRefreshToken(CustomUserDetails customUserDetails) {
        Instant expiration = Instant.now().plusMillis(refreshExpMs);
        String refreshToken = tokenProvider(customUserDetails, expiration);

        redisUtil.save(customUserDetails.getUsername(), refreshToken);

        return refreshToken;
    }

    // 제공된 리프레시 토큰을 기반으로 JwtDto 쌍을 다시 발급
    public JwtDto reissueToken(String refreshToken) throws SignatureException {

        // refreshToken 에서 user 정보를 가져와서 새로운 토큰을 발급 (발급 시간, 유효 시간(reset)만 새로 적용)
        CustomUserDetails userDetails = new CustomUserDetails(
                getEmail(refreshToken),
                null,
                getRoles(refreshToken)
        );

        // 재발급
        return new JwtDto(
                createJwtAccessToken(userDetails),
                createJwtRefreshToken(userDetails)
        );
    }

    // HTTP 요청의 'Authorization' 헤더에서 JWT 액세스 토큰을 검색
    public String resolveAccessToken(HttpServletRequest request) {
//        log.info("[ JwtUtil ] 헤더에서 토큰을 추출합니다.");
        String tokenFromHeader = request.getHeader("Authorization");

        if (tokenFromHeader == null || !tokenFromHeader.startsWith("Bearer ")) {
            log.warn("[ JwtUtil ] Request Header 에 토큰이 존재하지 않습니다.");
            return null;
        }

//        log.info("[ JwtUtil ] 헤더에 토큰이 존재합니다.");

        return tokenFromHeader.split(" ")[1]; //Bearer 와 분리
    }

    // 리프레시 토큰의 유효성을 검사
    public void isRefreshToken(String refreshToken) {
        String email = getEmail(refreshToken);

        Object foundRefreshToken = redisUtil.get(email);
        if (foundRefreshToken == null) {
            throw new IllegalArgumentException("Refresh Token 이 존재하지 않습니다.");
        }

        validateToken(foundRefreshToken.toString());
    }

    public void validateToken(String token) {
//        log.info("[ JwtUtil ] 토큰의 유효성을 검증합니다.");
        try {
            // 구문 분석 시스템의 시계가 JWT를 생성한 시스템의 시계 오차 고려
            // 약 3분 허용.
            long seconds = 3 *60;
            boolean isExpired = Jwts
                    .parser()
                    .clockSkewSeconds(seconds)
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getExpiration()
                    .before(new Date());
            if (isExpired) {
                log.info("만료된 JWT 토큰입니다.");
            }

        } catch (SecurityException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            //원하는 Exception throw
            throw new SecurityException("잘못된 토큰입니다.");
        } catch (ExpiredJwtException e) {
            //원하는 Exception throw
            throw new ExpiredJwtException(null, null, "만료된 JWT 토큰입니다.");
        }
    }
}
