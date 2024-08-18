package com.ds.deploysurfingbackend.global.config;

import com.ds.deploysurfingbackend.domain.user.repository.UserRepository;
import com.ds.deploysurfingbackend.global.filter.JwtAuthenticationFilter;
import com.ds.deploysurfingbackend.global.filter.CustomLogoutHandler;
import com.ds.deploysurfingbackend.global.filter.JwtAuthorizationFilter;
import com.ds.deploysurfingbackend.global.utils.HttpResponseUtil;
import com.ds.deploysurfingbackend.global.utils.JwtUtil;
import com.ds.deploysurfingbackend.global.utils.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration // 빈 등록
@EnableWebSecurity // 필터 체인 관리 시작 어노테이션
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthenticationConfiguration authenticationConfiguration;
    private final JwtUtil jwtUtil;

    private final RedisUtil redisUtil;

    private final UserRepository userRepository;


    //인증이 필요하지 않은 url
    private final String[] allowedUrls = {
            "/user/login", //로그인은 인증이 필요하지 않음
            "/user/signup", //회원가입은 인증이 필요하지 않음
            "/auth/reissue", //토큰 재발급은 인증이 필요하지 않음
            "api/usage",
            "/swagger-ui/**", //swagger 관련 URL
            "/v3/api-docs/**"
    };

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        // CORS 정책 설정
        http
                .cors(cors -> cors
                        .configurationSource(CorsConfig.corsConfigurationSource())
                );

        // csrf 비활성화
        http
                .csrf(AbstractHttpConfigurer::disable);

        // form 로그인 방식 비활성화 -> REST API 로그인
        http
                .formLogin(AbstractHttpConfigurer::disable);

        // http basic 인증 방식 비활성화
        http
                .httpBasic(AbstractHttpConfigurer::disable);

        // 세션을 사용하지 않음. (세션 생성 정책을 Stateless)
        http
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // 경로별 인가
        http
                .authorizeHttpRequests(auth -> auth
                        //위에서 정의했던 allowedUrls 들은 인증이 필요하지 않음 -> permitAll
                        .requestMatchers(allowedUrls).permitAll()
                        .anyRequest().authenticated() // 그 외의 url 들은 인증이 필요함
                );


        // Login Filter
        JwtAuthenticationFilter loginFilter = new JwtAuthenticationFilter(
                authenticationManager(authenticationConfiguration), jwtUtil);
        // Login Filter URL 지정
        loginFilter.setFilterProcessesUrl("/user/login");

        // filter chain 에 login filter 등록
        http
                .addFilterAt(loginFilter, UsernamePasswordAuthenticationFilter.class);
        // login filter 전에 Auth Filter 등록
        http
                .addFilterBefore(new JwtAuthorizationFilter(jwtUtil, redisUtil), JwtAuthenticationFilter.class);


        // JwtException 에 대한 Custom Exception 처리
//        http
//        .addFilterBefore(new JwtExceptionFilter(), JwtAuthorizationFilter.class);

        // Logout Filter
        http
                .logout(logout -> logout
                        .logoutUrl("/api/v1/organizations/logout")
                        .addLogoutHandler(new CustomLogoutHandler(redisUtil, jwtUtil))
                        .logoutSuccessHandler((request, response, authentication) ->
                                HttpResponseUtil.setSuccessResponse(
                                        response,
                                        HttpStatus.OK,
                                        "로그아웃 성공"
                                )
                        )
                );

        return http.build();
    }

}
