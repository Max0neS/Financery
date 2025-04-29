package com.example.financery.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {
    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    @Before("execution(* com.example.financery..*(..)) "
        + "&& !execution(* com.example.financery.mapper.TransactionMapper.*(..))")
    public void logBefore(JoinPoint joinPoint) {
        if (logger.isDebugEnabled()) {
            logger.info("Началось выполнение: {}", joinPoint.getSignature().toShortString());
        }
    }

    @AfterReturning(pointcut = "execution(* com.example.financery..*(..)) "
            + "&& !execution(* com.example.financery.mapper.TransactionMapper.*(..))",
            returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        if (logger.isDebugEnabled()) { 
            if (result instanceof ResponseEntity) {
                ResponseEntity<?> responseEntity = (ResponseEntity<?>) result;
                if (responseEntity.getStatusCode() == HttpStatus.BAD_REQUEST) {
                    logger.warn("Закончилось выполнение: {} с результатом: {}",
                            joinPoint.getSignature().toShortString(), result);
                    return;
                }
            }
            logger.info("Закончилось выполнение: {} с результатом: {}",
                    joinPoint.getSignature().toShortString(), result);
        }
    }

    @AfterThrowing(pointcut = "execution(* com.example.financery..*(..))", throwing = "error")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable error) {
        if (logger.isDebugEnabled()) {
            logger.error("Исключение в: {} с причиной: {}",
                    joinPoint.getSignature().toShortString(), error.getMessage());
        }
    }
}