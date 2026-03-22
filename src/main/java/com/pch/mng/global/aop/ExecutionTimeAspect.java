package com.pch.mng.global.aop;

import com.pch.mng.global.aop.annotation.ExecutionTime;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Aspect
@Component
@Slf4j
public class ExecutionTimeAspect {

    @Around("@annotation(executionTime)")
    public Object measure(ProceedingJoinPoint joinPoint, ExecutionTime executionTime)
            throws Throwable {
        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long elapsed = System.currentTimeMillis() - start;

        String label = StringUtils.hasText(executionTime.value())
                ? executionTime.value()
                : joinPoint.getSignature().toShortString();

        log.info("[실행시간] {} : {}ms", label, elapsed);
        return result;
    }
}
