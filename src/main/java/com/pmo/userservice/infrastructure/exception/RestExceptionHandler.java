package com.pmo.userservice.infrastructure.exception;

import com.pmo.common.exception.PmoExceptionHandler;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ControllerAdvice;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class RestExceptionHandler extends PmoExceptionHandler {
}