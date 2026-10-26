package com.example.onlinecourse.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Spring AOP Aspect for centralized logging and auditing across service & controller layers.
 * Demonstrates Syllabus Concept CO2 (Spring Boot + AOP + Logging).
 * Cross-cutting concern: intercepts service and controller execution, logs method entry,
 * execution timing, and errors without exposing sensitive information (e.g., passwords).
 */
@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    @Pointcut("execution(* com.example.onlinecourse.service..*(..))")
    public void serviceLayerPointcut() {}

    @Pointcut("execution(* com.example.onlinecourse.controller..*(..))")
    public void controllerLayerPointcut() {}

    @Pointcut("execution(* com.example.onlinecourse.service.RegistrationService.*(..))")
    public void registrationServicePointcut() {}

    @Before("serviceLayerPointcut()")
    public void logBeforeServiceMethod(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();
        // Mask or omit passwords in logs for security compliance
        String argsString = Arrays.stream(args)
                .map(arg -> {
                    if (arg == null) return "null";
                    String str = arg.toString();
                    if (str.toLowerCase().contains("password")) {
                        return "[PROTECTED]";
                    }
                    return str;
                })
                .reduce((a, b) -> a + ", " + b)
                .orElse("none");

        logger.info("[AOP-BEFORE] Service execution started: {} with args: [{}]", methodName, argsString);
    }

    @AfterReturning(pointcut = "serviceLayerPointcut()", returning = "result")
    public void logAfterReturningServiceMethod(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().toShortString();
        logger.info("[AOP-AFTER-RETURNING] Service method executed successfully: {}", methodName);
    }

    @AfterThrowing(pointcut = "serviceLayerPointcut()", throwing = "exception")
    public void logAfterThrowingServiceMethod(JoinPoint joinPoint, Throwable exception) {
        String methodName = joinPoint.getSignature().toShortString();
        logger.error("[AOP-AFTER-THROWING] Exception occurred in service method: {} - Error: {}",
                methodName, exception.getMessage());
    }

    /**
     * Around advice specifically measuring the transaction duration of critical registration operations.
     */
    @Around("registrationServicePointcut()")
    public Object profileRegistrationOperations(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().toShortString();
        logger.info("[AOP-AROUND] Starting transactional registration profile for: {}", methodName);

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            logger.info("[AOP-AROUND] Completed {} in {} ms (Transaction committed)", methodName, duration);
            return result;
        } catch (Throwable ex) {
            long duration = System.currentTimeMillis() - startTime;
            logger.warn("[AOP-AROUND] Failed {} after {} ms (Transaction rollback triggered): {}",
                    methodName, duration, ex.getMessage());
            throw ex;
        }
    }
}
