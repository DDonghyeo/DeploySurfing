package com.ds.deploysurfingbackend.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean
    public ThreadPoolTaskExecutor createTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2); //기본 스레드 풀 크기. 항상 활성 상태로 유지되는 스레드 수. 보통 CPU 코어 수와 비슷하게 설정.
        executor.setMaxPoolSize(4); //동시에 실행될 수 있는 최대 스레드 수. 보통 corePoolSize 2배로 설정
        executor.setQueueCapacity(50); //작업 대기열 크기. 일반적으로 50~500 값을 사용
        executor.setThreadNamePrefix("AsyncThread-"); //스레드 이름 접두사
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy()); //대기열이 가득 찼을 때 정책 -> 호출한 스레드에서 처리하도록 설정
        executor.initialize();
        return executor;
    }
}
