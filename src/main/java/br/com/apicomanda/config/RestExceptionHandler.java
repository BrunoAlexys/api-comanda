package br.com.apicomanda.config;

import br.com.apicomanda.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

@RestControllerAdvice
@Slf4j
public class RestExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ExceptionDetails handlerNotFounException(NotFoundException exception) {
        log.info("NotFoundException: {}", exception.getMessage());
        return ExceptionDetails.builder()
                .timestamp(LocalDateTime.now())
                .status(NOT_FOUND.value())
                .title(NOT_FOUND.name())
                .details(exception.getMessage())
                .developerMessage(exception.getClass().getName())
                .build();
    }

    @ExceptionHandler(ObjectAlreadyRegisteredException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ExceptionDetails handlerObjectAlreadyRegisteredException(ObjectAlreadyRegisteredException exception) {
        log.info("ObjectAlreadyRegisteredException: {}", exception.getMessage());
        return ExceptionDetails.builder()
                .timestamp(LocalDateTime.now())
                .status(CONFLICT.value())
                .title(CONFLICT.name())
                .details(exception.getMessage())
                .developerMessage(exception.getClass().getName())
                .build();
    }

    @ExceptionHandler(UserInactiveException.class)
    @ResponseStatus(UNPROCESSABLE_ENTITY)
    public ExceptionDetails handlerUserInactiveException(UserInactiveException exception) {
        log.info("UserInactiveException: {}", exception.getMessage());
        return ExceptionDetails.builder()
                .timestamp(LocalDateTime.now())
                .status(UNPROCESSABLE_ENTITY.value())
                .title(UNPROCESSABLE_ENTITY.name())
                .details(exception.getMessage())
                .developerMessage(exception.getClass().getName())
                .build();
    }

    @ExceptionHandler(UserUnauthorizedExecption.class)
    @ResponseStatus(UNAUTHORIZED)
    public ExceptionDetails handlerUserUnauthorizedException(UserUnauthorizedExecption exception) {
        log.info("UserUnauthorizedException: {}", exception.getMessage());
        return ExceptionDetails.builder()
                .timestamp(LocalDateTime.now())
                .status(UNAUTHORIZED.value())
                .title(UNAUTHORIZED.name())
                .details(exception.getMessage())
                .developerMessage(exception.getClass().getName())
                .build();
    }
}
