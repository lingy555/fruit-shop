package com.lingnan.fruitshop.common.logging;

import com.lingnan.fruitshop.common.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class ExceptionLoggingAspect {

    @Around("execution(* com.lingnan.fruitshop.controller..*(..)) || execution(* com.lingnan.fruitshop.service..*(..))")
    public Object logOnException(ProceedingJoinPoint pjp) throws Throwable {
        try {
            return pjp.proceed();
        } catch (BizException e) {
            MethodSignature sig = (MethodSignature) pjp.getSignature();
            log.warn("[BizException] {}.{}({}) code={} msg={}",
                    sig.getDeclaringType().getSimpleName(),
                    sig.getName(),
                    formatArgs(pjp.getArgs()),
                    e.getCode(),
                    e.getMessage());
            throw e;
        } catch (Throwable e) {
            MethodSignature sig = (MethodSignature) pjp.getSignature();
            log.error("[UnhandledException] {}.{}({})",
                    sig.getDeclaringType().getSimpleName(),
                    sig.getName(),
                    formatArgs(pjp.getArgs()),
                    e);
            throw e;
        }
    }

    private String formatArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(sanitizeArg(args[i]));
            if (sb.length() > 1500) {
                sb.setLength(1500);
                sb.append("...");
                break;
            }
        }
        return sb.toString();
    }

    private String sanitizeArg(Object arg) {
        if (arg == null) {
            return "null";
        }
        if (arg instanceof HttpServletRequest) {
            return "HttpServletRequest";
        }
        if (arg instanceof HttpServletResponse) {
            return "HttpServletResponse";
        }

        Class<?> clazz = arg.getClass();
        String typeName = clazz.getSimpleName();

        if (looksSensitive(clazz)) {
            return typeName + "{***}";
        }

        String s;
        try {
            s = String.valueOf(arg);
        } catch (Exception e) {
            s = typeName + "{toStringError=" + e.getClass().getSimpleName() + "}";
        }

        if (s.length() > 800) {
            s = s.substring(0, 800) + "...";
        }
        return s;
    }

    private boolean looksSensitive(Class<?> clazz) {
        String name = clazz.getSimpleName().toLowerCase();
        if (name.contains("password") || name.contains("pwd")) {
            return true;
        }
        try {
            for (Method m : clazz.getMethods()) {
                String mn = m.getName().toLowerCase();
                if (mn.equals("getpassword") || mn.equals("getnewpassword") || mn.equals("getoldpassword") || mn.contains("password")) {
                    return true;
                }
            }
        } catch (Exception ignore) {
        }
        return false;
    }
}
