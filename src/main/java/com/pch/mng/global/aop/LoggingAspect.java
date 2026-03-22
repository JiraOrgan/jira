package com.pch.mng.global.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Pointcut("execution(* com.pch.mng.domain.*.controller.*.*(..))")
    private void controllerMethods() {}

    @Pointcut("execution(* com.pch.mng.domain.*.service.*.*(..))")
    private void serviceMethods() {}

    @Around("controllerMethods()")
    public Object logController(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().toShortString();

        log.info("[API 시작] {}", methodName);

        try {
            Object result = joinPoint.proceed();
            long elapsed = System.currentTimeMillis() - start;
            log.info("[API 완료] {} - {}ms", methodName, elapsed);
            return result;
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start;
            log.error("[API 에러] {} - {}ms | {}", methodName, elapsed, e.getMessage());
            throw e;
        }
    }

    @Around("serviceMethods()")
    public Object logSlowService(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long elapsed = System.currentTimeMillis() - start;

        if (elapsed > 1000) {
            log.warn("[SLOW] {} - {}ms", joinPoint.getSignature().toShortString(), elapsed);
        }
        return result;
    }
}
