package com.lingnan.fruitshop.common.exception;

import com.lingnan.fruitshop.common.api.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public ApiResponse<Void> handleBizException(BizException e, HttpServletRequest request) {
        log.warn("[BizException] {} {} ip={} code={} msg={}", request.getMethod(), request.getRequestURI(), request.getRemoteAddr(), e.getCode(), e.getMessage());
        return ApiResponse.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class, ConstraintViolationException.class})
    public ApiResponse<Void> handleValidationException(Exception e, HttpServletRequest request) {
        String detail = buildValidationDetail(e);
        log.warn("[ValidationException] {} {} ip={} detail={}", request.getMethod(), request.getRequestURI(), request.getRemoteAddr(), detail);
        return ApiResponse.fail(400, detail);
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception e, HttpServletRequest request) {
        log.error("[UnhandledException] {} {} ip={}", request.getMethod(), request.getRequestURI(), request.getRemoteAddr(), e);
        return ApiResponse.fail(500, "服务器内部错误");
    }

    private String buildValidationDetail(Exception e) {
        try {
            if (e instanceof MethodArgumentNotValidException ex) {
                List<FieldError> errors = ex.getBindingResult().getFieldErrors();
                if (errors == null || errors.isEmpty()) {
                    return "请求参数错误";
                }
                return "请求参数错误: " + errors.stream()
                        .map(err -> err.getField() + " " + (err.getDefaultMessage() == null ? "" : err.getDefaultMessage()))
                        .collect(Collectors.joining("; "));
            }
            if (e instanceof BindException ex) {
                List<FieldError> errors = ex.getBindingResult().getFieldErrors();
                if (errors == null || errors.isEmpty()) {
                    return "请求参数错误";
                }
                return "请求参数错误: " + errors.stream()
                        .map(err -> err.getField() + " " + (err.getDefaultMessage() == null ? "" : err.getDefaultMessage()))
                        .collect(Collectors.joining("; "));
            }
            if (e instanceof ConstraintViolationException ex) {
                if (ex.getConstraintViolations() == null || ex.getConstraintViolations().isEmpty()) {
                    return "请求参数错误";
                }
                return "请求参数错误: " + ex.getConstraintViolations().stream()
                        .map(v -> (v.getPropertyPath() == null ? "" : v.getPropertyPath().toString()) + " " + (v.getMessage() == null ? "" : v.getMessage()))
                        .collect(Collectors.joining("; "));
            }
        } catch (Exception ignore) {
        }
        return "请求参数错误";
    }
}
