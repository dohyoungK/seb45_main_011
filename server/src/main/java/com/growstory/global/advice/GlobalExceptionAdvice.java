package com.growstory.global.advice;

import com.growstory.global.exception.BusinessLogicException;
import com.growstory.global.response.ErrorResponder;
import com.growstory.global.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;
import java.io.IOException;

@RestControllerAdvice
public class GlobalExceptionAdvice {
    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    // HTTP 요청의 파라미터나 바디에 대한 유효성 검사 예외 처리
    public ErrorResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        return ErrorResponse.of(e.getBindingResult());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    // 엔티티 클래스의 필드에 대한 유효성 검사 예외 처리
    public ErrorResponse handleConstraintViolationException(ConstraintViolationException e) {
        return ErrorResponse.of(e.getConstraintViolations());
    }

    @ExceptionHandler(BusinessLogicException.class)
    // BusinessLogicException 처리
    public ResponseEntity handleBusinessLogicException(BusinessLogicException e) throws IOException {
        final ErrorResponse errorResponse = ErrorResponse.of(e.getExceptionCode());

//        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getResponse();
//        ErrorResponder.sendErrorResponse(response, e.getExceptionCode().getStatus() ,e.getExceptionCode().getMessage());

        return new ResponseEntity(errorResponse, HttpStatus.valueOf(e.getExceptionCode().getStatus()));
    }
}
