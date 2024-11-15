package com.authorizer.infrastructure.handler;


import com.authorizer.domain.enums.AuthorizationStatusEnum;
import com.authorizer.domain.exception.EntityNotFoundException;
import com.authorizer.domain.exception.InsufficientBalanceException;
import com.authorizer.infrastructure.services.MessageService;
import com.authorizer.presentation.dto.transaction.TransactionResponseDTO;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ArrayList;
import java.util.List;

@Log4j2
@ControllerAdvice
public class ApplicationExceptionHandler extends ResponseEntityExceptionHandler {

    protected final MessageService messageService;

    public ApplicationExceptionHandler(MessageService messageService) {
        this.messageService = messageService;
    }


    @ExceptionHandler({RuntimeException.class})
    public ResponseEntity<Object> handleRuntimeException(RuntimeException ex, WebRequest request) {
        log.error("HttpStatus {} handleRuntimeException: {}", HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex);
        return handleException(ex, AuthorizationStatusEnum.ERROR, new HttpHeaders(), request);
    }

    @ExceptionHandler({InsufficientBalanceException.class})
    public ResponseEntity<Object> handleInsufficientBalanceException(RuntimeException ex, WebRequest request) {
        log.debug("HttpStatus {} handleInsufficientBalanceException: {}", HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), ex);
        return handleException(ex, AuthorizationStatusEnum.REJECTED, new HttpHeaders(), request);
    }

    @ExceptionHandler({EntityNotFoundException.class})
    public ResponseEntity<Object> handleEntityNotFoundException(RuntimeException ex, WebRequest request) {
        log.debug("HttpStatus {} handleBalanceTypeNotFoundException: {}", HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        return handleException(ex, AuthorizationStatusEnum.ERROR, new HttpHeaders(), request);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<String> errors = getErrorList(ex.getBindingResult());
        log.debug("HttpStatus {} handleMethodArgumentNotValid: {}", HttpStatus.BAD_REQUEST, errors.toString());
        return handleException(ex, AuthorizationStatusEnum.ERROR, headers, request);
    }

    protected List<String> getErrorList(BindingResult bindingResult) {
        List<String> erros = new ArrayList<>();
        bindingResult.getFieldErrors().forEach(e -> erros.add(e.getField() + " " + messageService.getMessage(e)));
        return erros;
    }

    protected ResponseEntity<Object> handleException(Exception ex, AuthorizationStatusEnum authorizationStatusEnum, HttpHeaders httpHeaders, WebRequest request) {
        TransactionResponseDTO response = new TransactionResponseDTO(authorizationStatusEnum.getCode());
        return handleExceptionInternal(ex, response, httpHeaders, HttpStatus.OK, request);
    }

}