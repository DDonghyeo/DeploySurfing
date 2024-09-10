package com.ds.deploysurfingbackend.global.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RedissonLock {

    String value(); //락 키 생성에 사용되는 SpEL 표현

    long waitTime() default 5000L; // 락 획득 대기 시간, 5초

    long leaseTime() default 3000L; // 락 유효 시간, 3초
}
