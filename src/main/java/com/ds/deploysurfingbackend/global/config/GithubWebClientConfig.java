package com.ds.deploysurfingbackend.global.config;

import com.ds.deploysurfingbackend.global.exception.CommonErrorCode;
import com.ds.deploysurfingbackend.global.exception.CustomException;
import io.netty.channel.ChannelOption;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class GithubWebClientConfig {

    private static final String GITHUB_API_URL = "https://api.github.com/";

    @Bean
    public WebClient githubWebClient() {
        return WebClient.builder()
                .baseUrl(GITHUB_API_URL)
                .defaultHeader("X-GitHub-Api-Version", "2022-11-28")
                .filter(this::addAuthorizationHeader)
                .filter(this::handleErrors)
                .build();
    }

    //권한 토큰 필터
    private Mono<ClientResponse> addAuthorizationHeader(ClientRequest request, ExchangeFunction next) {
        String token = request.headers().getFirst("Authorization");
        if (token == null) {
            return next.exchange(request);
        }

        ClientRequest authorizedRequest = ClientRequest.from(request)
                .header("Authorization", token.startsWith("Bearer ") ? token : "Bearer " + token)
                .build();
        return next.exchange(authorizedRequest);
    }

    private Mono<ClientResponse> handleErrors(ClientRequest request, ExchangeFunction next) {
        return next.exchange(request)
                .flatMap(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return Mono.just(response);
                    }

                    //200 OK
                    //201 Created
                    //404 Resource not found
                    //409 Conflict
                    //422 Validation failed, or the endpoint has been spammed.
                    if (response.statusCode().is4xxClientError()) {
                        return Mono.error(new CustomException(CommonErrorCode.SERVER_ERROR));
                    }

                    return response.createException()
                            .flatMap(Mono::error);
                });
    }

//    HttpClient httpClient = HttpClient.create()
//            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000); // 10초

//    public ConnectionProvider connectionProvider() {
//        return ConnectionProvider.builder("http")
//                .maxConnections(50) // Connection Pool 개수
//                .pendingAcquireTimeout(Duration.ofMillis(0)) // Connection 을 얻기 위해 기다리는 최대 시간
//                .pendingAcquireMaxCount(-1) // Connection을 가져오는 시도 횟수, -1은 제한 없음
//                .maxIdleTime(Duration.ofMillis(1000L)) // idle 상태의 커넥션을 유지하는 시간
//                .build();
//
//    }
}
