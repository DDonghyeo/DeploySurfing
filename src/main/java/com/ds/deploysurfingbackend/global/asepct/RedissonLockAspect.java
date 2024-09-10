package com.ds.deploysurfingbackend.global.asepct;

import com.ds.deploysurfingbackend.global.annotation.RedissonLock;
import com.ds.deploysurfingbackend.global.exception.CommonErrorCode;
import com.ds.deploysurfingbackend.global.exception.CustomException;
import com.ds.deploysurfingbackend.global.utils.RedisLockSpELParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RedissonLockAspect {

    private final RedissonClient redissonClient;

    @Around("@annotation(com.ds.deploysurfingbackend.global.annotation.RedissonLock)")
    public Object redissonLock(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RedissonLock redissonLock = method.getAnnotation(RedissonLock.class);

        //락 키 생성
        String lockKey =
                method.getName() + ":" + RedisLockSpELParser.getLockKey(signature.getParameterNames(),
                        joinPoint.getArgs(), redissonLock.value());

        long waitTime = redissonLock.waitTime();
        long leaseTime = redissonLock.leaseTime();

        RLock lock = redissonClient.getLock(lockKey);
        boolean isLocked = false;

        try {
            isLocked = lock.tryLock(waitTime, leaseTime, MILLISECONDS);
            if (isLocked) {
                log.info("[Redisson Lock] 락 획득 성공 ---> {}", lockKey);
                return joinPoint.proceed();
            } else {
                log.error("[Redisson Lock] 락 획득 실패 ---> {}", lockKey);
                throw new CustomException(CommonErrorCode.FAILED_TO_ACQUIRE_LOCK);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[Redisson Lock] 락 획득 중 인터럽트 발생 ---> {}", lockKey);
            throw new CustomException(CommonErrorCode.FAILED_TO_ACQUIRE_LOCK);
        } finally {
            if (isLocked) {
                lock.unlock();
                log.info("[Redisson Lock] 락을 해제하는데 성공 ---> {}", lockKey);
            }
        }
    }

}
