package com.shortlink.common.aop;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Parameter;

/**
 * 统一日志切面：拦截Controller和Service层方法，记录入参、出参、耗时。
 * 超过500ms的方法会以WARN级别标记慢查询。
 */
@Slf4j
@Aspect
@Component
public class LogAspect {

    /** 拦截所有Controller方法 */
    @Around("execution(* com.shortlink.*.controller..*(..))")
    public Object logController(ProceedingJoinPoint joinPoint) throws Throwable {
        return logMethod(joinPoint, "CONTROLLER");
    }

    /** 拦截所有Service实现类方法 */
    @Around("execution(* com.shortlink.*.service.impl..*(..))")
    public Object logService(ProceedingJoinPoint joinPoint) throws Throwable {
        return logMethod(joinPoint, "SERVICE");
    }

    private Object logMethod(ProceedingJoinPoint joinPoint, String layer) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();
        Object[] args = joinPoint.getArgs();

        // 过滤敏感参数（如HttpServletRequest）避免日志过大
        String params = buildParams(signature.getMethod().getParameters(), args);

        long start = System.currentTimeMillis();
        Object result;
        try {
            result = joinPoint.proceed();
            long elapsed = System.currentTimeMillis() - start;
            if (elapsed > 500) {
                log.warn("[{}] {}.{} params={} result={} elapsed={}ms (SLOW)",
                        layer, className, methodName, params, truncate(JSON.toJSONString(result)), elapsed);
            } else {
                log.info("[{}] {}.{} params={} result={} elapsed={}ms",
                        layer, className, methodName, params, truncate(JSON.toJSONString(result)), elapsed);
            }
            return result;
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start;
            log.error("[{}] {}.{} params={} error={} elapsed={}ms",
                    layer, className, methodName, params, e.getMessage(), elapsed);
            throw e;
        }
    }

    private String buildParams(Parameter[] parameters, Object[] args) {
        if (args == null || args.length == 0) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < args.length; i++) {
            if (i > 0) sb.append(", ");
            // 跳过HttpServletRequest/Response避免日志过大
            if (args[i] instanceof jakarta.servlet.http.HttpServletRequest) {
                sb.append(parameters[i].getName()).append("=HttpServletRequest");
            } else if (args[i] instanceof jakarta.servlet.http.HttpServletResponse) {
                sb.append(parameters[i].getName()).append("=HttpServletResponse");
            } else {
                sb.append(parameters[i].getName()).append("=").append(truncate(JSON.toJSONString(args[i])));
            }
        }
        sb.append("]");
        return sb.toString();
    }

    /** 截断过长字符串，防止日志爆炸 */
    private String truncate(String s) {
        if (s == null) return "null";
        return s.length() > 500 ? s.substring(0, 500) + "..." : s;
    }
}
