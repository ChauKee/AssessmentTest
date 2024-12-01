package com.vanguard.assessment.controller;

import org.apache.tomcat.util.ExceptionUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.ControllerAdviceBean;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class ControllerAdvisor {
//        extends ResponseEntityExceptionHandler {

    @ExceptionHandler({HandlerMethodValidationException.class})
    public ResponseEntity<String> handle(HandlerMethodValidationException e) throws Exception {
        System.out.println(e.getMessage());
        e.printStackTrace();
        throw e;
    }
}
