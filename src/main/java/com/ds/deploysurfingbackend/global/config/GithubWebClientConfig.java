package com.ds.deploysurfingbackend.global.config;

import com.ds.deploysurfingbackend.domain.github.dto.GithubErrorResponse;
import com.ds.deploysurfingbackend.domain.github.exception.GithubErrorCode;
import com.ds.deploysurfingbackend.global.exception.CommonErrorCode;
import com.ds.deploysurfingbackend.global.exception.CustomException;
import io.netty.channel.ChannelOption;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Configuration
@RequiredArgsConstructor
public class GithubWebClientConfig {

    private static final String GITHUB_API_URL = "https://api.github.com/";

    @Bean
    public WebClient githubWebClient() {
        return WebClient.builder()
                .baseUrl(GITHUB_API_URL)
                .defaultHeader("X-GitHub-Api-Version", "2022-11-28")
                .defaultHeader("Accept", "application/vnd.github+json")
                .filter(this::handleErrors)
                .build();
    }

    private Mono<ClientResponse> handleErrors(ClientRequest request, ExchangeFunction next) {
        return next.exchange(request)
                .flatMap(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return Mono.just(response);
                    }

                    return response.bodyToMono(GithubErrorResponse.class).flatMap(githubErrorResponse -> {
                        log.error(githubErrorResponse.message());
                        return Mono.error(new CustomException(GithubErrorCode.GITHUB_METADATA_NOT_FOUND));
                    });


//                    if (response.statusCode().is4xxClientError()) {
//                        if (response.statusCode().value() == 401) {
//                            return Mono.error(new CustomException(GithubErrorCode.GITHUB_METADATA_NOT_FOUND));
//                        } else if (response.statusCode().value() == 403) {
//                            return Mono.error(new CustomException(GithubErrorCode.INVALID_GITHUB_TOKEN));
//                        } else if (response.statusCode().value() == 404) {
//                            return Mono.error(new CustomException(GithubErrorCode.GITHUB_METADATA_NOT_FOUND));
//                        } else {
//                            return Mono.error(new CustomException(CommonErrorCode.SERVER_ERROR));
//                        }
//                    }
//
//                    return response.createException()
//                            .flatMap(Mono::error);
//                });
                });
    }
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

